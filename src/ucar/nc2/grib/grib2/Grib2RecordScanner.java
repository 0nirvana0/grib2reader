/*
 * Copyright 1998-2015 John Caron and University Corporation for Atmospheric Research/Unidata
 *
 *  Portions of this software were developed by the Unidata Program at the
 *  University Corporation for Atmospheric Research.
 *
 *  Access and use of this software shall impose the following obligations
 *  and understandings on the user. The user is granted the right, without
 *  any fee or cost, to use, copy, modify, alter, enhance and distribute
 *  this software, and any derivative works thereof, and its supporting
 *  documentation for any purpose whatsoever, provided that this entire
 *  notice appears in all copies of the software, derivative works and
 *  supporting documentation.  Further, UCAR requests that the user credit
 *  UCAR/Unidata in any publications that result from the use of this
 *  software or in any product that includes this software. The names UCAR
 *  and/or Unidata, however, may not be used in any advertising or publicity
 *  to endorse or promote any products or commercial entity unless specific
 *  written permission is obtained from UCAR/Unidata. The user also
 *  understands that UCAR/Unidata is not obligated to provide the user with
 *  any support, consulting, training or assistance of any kind with regard
 *  to the use, operation and performance of this software nor to provide
 *  the user with any updates, revisions, new versions or "bug fixes."
 *
 *  THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *  WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package ucar.nc2.grib.grib2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ucar.nc2.grib.GribNumbers;
import ucar.unidata.io.KMPMatch;
import ucar.unidata.io.RandomAccessFile;
import ucar.unidata.util.StringUtil2;

/**
 * Scan raf for grib-2 messages
 *
 * @author caron
 * @since 3/28/11
 */
public class Grib2RecordScanner {
	// static private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Grib2RecordScanner.class);
	static private final KMPMatch matcher = new KMPMatch(new byte[] { 'G', 'R', 'I', 'B' });
	static private final int maxScan = 16000;

	static public boolean isValidFile(RandomAccessFile raf) {
		try {
			raf.seek(0);
			boolean found = raf.searchForward(matcher, maxScan); // look in first 16K
			if (!found)
				return false;
			raf.skipBytes(7); // will be positioned on byte 0 of indicator section
			int edition = raf.read(); // read at byte 8
			if (edition != 2)
				return false;

			// check ending = 7777
			long len = GribNumbers.int8(raf);
			if (len > raf.length())
				return false;
			raf.skipBytes(len - 20);
			for (int i = 0; i < 4; i++) {
				if (raf.read() != 55)
					return false;
			}
			return true;

		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * tricky bit of business. recapture the entire record based on drs position.
	 * for validation.
	 * @param raf             from this RandomAccessFile
	 * @param drsPos          Grib2SectionDataRepresentation starts here
	 */
	public static Grib2Record findRecordByDrspos(RandomAccessFile raf, long drsPos) throws IOException {
		long pos = Math.max(0, drsPos - (20 * 1000)); // go back 20K
		Grib2RecordScanner scan = new Grib2RecordScanner(raf, pos);
		while (scan.hasNext()) {
			ucar.nc2.grib.grib2.Grib2Record gr = scan.next();
			Grib2SectionDataRepresentation drs = gr.getDataRepresentationSection();
			if (drsPos == drs.getStartingPosition())
				return gr;
			if (raf.getFilePointer() > drsPos)
				break; // missed it.
		}
		return null;
	}

	// ////////////////////////////////////////////////////////////////////////////
	private Map<Long, Grib2SectionGridDefinition> gdsMap = new HashMap<>();
	private ucar.unidata.io.RandomAccessFile raf = null;

	private byte[] header;
	// private int badEndings = 0;
	private long lastPos = 0; // start scanning from here

	// deal with repeating sections - each becomes a Grib2Record
	private long repeatPos = -1; // if > 0, we are in middle of repeating record
	private Grib2Record repeatRecord = null; // current repeating record
	private Grib2SectionBitMap repeatBms = null; // current repeating bms

	public Grib2RecordScanner(RandomAccessFile raf) throws IOException {
		this.raf = raf;
		raf.seek(0);
		raf.order(RandomAccessFile.BIG_ENDIAN);
		lastPos = 0;
	}

	private Grib2RecordScanner(RandomAccessFile raf, long startFrom) throws IOException {
		this.raf = raf;
		raf.seek(startFrom);
		raf.order(RandomAccessFile.BIG_ENDIAN);
		lastPos = startFrom;
	}

	public boolean hasNext() throws IOException {
		if (lastPos >= raf.length()) {
			return false;
		}
		if (repeatPos > 0) {
			if (nextRepeating()) // this has created a new repeatRecord
				return true;
		} else {
			repeatRecord = null;
			repeatBms = null;
			// fall through to new record
		}
		boolean more;
		long gribStart = 0;
		// 数据里是不包含 GRIB 跳出
		while (true) { // scan until we get a GRIB-2 or more == false
			raf.seek(lastPos);
			// 判断数据里是否包含 GRIB
			more = raf.searchForward(matcher, -1); // will scan to end for a 'GRIB' string
			if (!more) {
				break;
			}

			gribStart = raf.getFilePointer(); // this is where the next 'GRIB' starts
			// see if its GRIB-2
			raf.skipBytes(7);
			int edition = raf.read();
			if (edition == 2)
				break;
			lastPos = raf.getFilePointer(); // not edition 2 ! just skip it !! start scanning from there
			System.err.println("GRIB message at pos=" + gribStart + " not GRIB2; skip");
		}

		if (more) {
			int sizeHeader = (int) (gribStart - lastPos); // wmo headers are embedded between records in some idd streams
			long goBack = gribStart - sizeHeader;
			header = new byte[sizeHeader];
			raf.seek(goBack);
			raf.readFully(header);
			raf.seek(gribStart);
			this.lastPos = gribStart; // ok start from here next time
		}
		return more;
	}

	public Grib2Record next() throws IOException {
		if (repeatRecord != null) { // serve current repeatRecord if it exists
			return new Grib2Record(repeatRecord);
		}
		Grib2SectionIndicator is = null;
		try {
			is = new Grib2SectionIndicator(raf);
			Grib2SectionIdentification ids = new Grib2SectionIdentification(raf);
			Grib2SectionLocalUse lus = new Grib2SectionLocalUse(raf);
			Grib2SectionGridDefinition gds = new Grib2SectionGridDefinition(raf);
			Grib2SectionProductDefinition pds = new Grib2SectionProductDefinition(raf);
			Grib2SectionDataRepresentation drs = new Grib2SectionDataRepresentation(raf);
			Grib2SectionBitMap bms = new Grib2SectionBitMap(raf);
			Grib2SectionData dataSection = new Grib2SectionData(raf);
			if (dataSection.getMsgLength() > is.getMessageLength()) { // presumably corrupt
				throw new IllegalStateException("Illegal Grib2SectionData Message Length");
			}

			// look for duplicate gds
			long crc = gds.calcCRC();
			Grib2SectionGridDefinition gdsCached = gdsMap.get(crc);
			if (gdsCached != null)
				gds = gdsCached; // hmmmm why ??
			else
				gdsMap.put(crc, gds);

			// check to see if we have a repeating record
			long pos = raf.getFilePointer();
			long ending = is.getEndPos();
			if (pos + 34 < ending) { // give it 30 bytes of slop
				repeatPos = pos;
				repeatRecord = new Grib2Record(header, is, ids, lus, gds, pds, drs, bms, dataSection, false, Grib2Index.ScanModeMissing); // this assumes immutable sections
				// track bms in case its a repeat
				if (bms.getBitMapIndicator() == 0)
					repeatBms = bms;
				return new Grib2Record(repeatRecord); // GribRecord isnt immutable; still, may not be necessary
			}

			// check that end section is correct
			boolean foundEnding = true;
			// 是不是7777
			raf.seek(ending - 4);
			for (int i = 0; i < 4; i++) {
				if (raf.read() != 55) {
					foundEnding = false;
					break;
				}
			}

			if (foundEnding) {
				lastPos = raf.getFilePointer();
				return new Grib2Record(header, is, ids, lus, gds, pds, drs, bms, dataSection, false, Grib2Index.ScanModeMissing);

			} else { // skip this record
				// lastPos = is.getEndPos() + 20; dont trust is.getEndPos()
				lastPos += 20; // skip "GRIB"
				if (hasNext()) // search forward for another one
					return next();
			}
		} catch (Throwable t) {
			long pos = (is == null) ? -1 : is.getStartPos();
			System.err.println("Bad GRIB2 record in file {}, skipping pos={} cause={}" + raf.getLocation() + " " + pos + " " + t.getMessage());
			lastPos += 20; // skip "GRIB"
			if (hasNext()) // search forward for another one
				return next();
		}

		return null; // last record was incomplete
	}

	// return true if got another repeat out of this record
	// side effect is that the new record is in repeatRecord
	private boolean nextRepeating() throws IOException {
		raf.seek(repeatPos);
		GribNumbers.int4(raf); // skip octets 1-4
		int section = raf.read(); // find out what section this is
		raf.seek(repeatPos); // back to beginning of section
		if (section == 2) {
			repeatRecord.setLus(new Grib2SectionLocalUse(raf));
			repeatRecord.setGdss(new Grib2SectionGridDefinition(raf));
			repeatRecord.setPdss(new Grib2SectionProductDefinition(raf));
			repeatRecord.setDrs(new Grib2SectionDataRepresentation(raf));
			repeatRecord.setBms(new Grib2SectionBitMap(raf), false);
			repeatRecord.setDataSection(new Grib2SectionData(raf));
			repeatRecord.repeat = section;

		} else if (section == 3) {
			repeatRecord.setGdss(new Grib2SectionGridDefinition(raf));
			repeatRecord.setPdss(new Grib2SectionProductDefinition(raf));
			repeatRecord.setDrs(new Grib2SectionDataRepresentation(raf));
			repeatRecord.setBms(new Grib2SectionBitMap(raf), false);
			repeatRecord.setDataSection(new Grib2SectionData(raf));
			repeatRecord.repeat = section;

		} else if (section == 4) {
			repeatRecord.setPdss(new Grib2SectionProductDefinition(raf));
			repeatRecord.setDrs(new Grib2SectionDataRepresentation(raf));
			repeatRecord.setBms(new Grib2SectionBitMap(raf), false);
			repeatRecord.setDataSection(new Grib2SectionData(raf));
			repeatRecord.repeat = section;

		} else {
			lastPos = repeatPos; // start next scan from here
			repeatPos = -1;
			repeatRecord = null;
			repeatBms = null;
			return false;
		}

		// look for repeating bms
		Grib2SectionBitMap bms = repeatRecord.getBitmapSection();
		if (bms.getBitMapIndicator() == 254) {
			// replace BMS with last good one
			if (repeatBms == null)
				throw new IllegalStateException("No bms in repeating section");
			repeatRecord.setBms(repeatBms, true);
			repeatRecord.repeat += 1000;

		} else if (bms.getBitMapIndicator() == 0) {
			// track last good bms
			repeatBms = repeatRecord.getBitmapSection();
		}

		// keep only unique gds
		if ((section == 2) || (section == 3)) {
			// look for duplicate gds
			Grib2SectionGridDefinition gds = repeatRecord.getGDSsection();
			long crc = gds.calcCRC();
			Grib2SectionGridDefinition gdsCached = gdsMap.get(crc);
			if (gdsCached != null)
				repeatRecord.setGdss(gdsCached);
			else
				gdsMap.put(crc, gds);
		}

		// check to see if we are at the end
		long pos = raf.getFilePointer();
		long ending = repeatRecord.getIs().getEndPos();
		if (pos + 34 < ending) { // give it 30 bytes of slop
			repeatPos = pos;
			return true;
		}

		// check that end section is correct
		raf.seek(ending - 4);
		for (int i = 0; i < 4; i++) {
			if (raf.read() != 55) {
				String clean = StringUtil2.cleanup(header);
				if (clean.length() > 40)
					clean = clean.substring(0, 40) + "...";
				break;
			}
		}
		lastPos = raf.getFilePointer();
		repeatPos = -1; // no more repeats in this record
		return true;
	}

}
