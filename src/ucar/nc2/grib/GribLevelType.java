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

package ucar.nc2.grib;



/**
* Encapsulate the semantics in GRIB level types  (Grib1 table 3, Grib2 code table 4.5).
* These may be dependent on the center, so must be generated by a GribCustomizer.
 *
* @author caron
* @since 1/16/12
*/

public class GribLevelType implements VertCoord.VertUnit {

  private final int code;
  private final String desc;
  private final String abbrev;
  private final String units;
  private final String datum;
  private final boolean isPositiveUp;
  private final boolean isLayer;

  // LOOK for Grib2Utils - CHANGE THIS
  public GribLevelType(int code, String units, String datum, boolean isPositiveUp) {
    this.code = code;
    this.desc = null;
    this.abbrev = null;
    this.units = units;
    this.datum = datum;
    this.isPositiveUp = isPositiveUp;
    this.isLayer = false;
  }

  public GribLevelType(int code, String desc, String abbrev, String units, String datum, boolean isPositiveUp, boolean isLayer) {
    this.code = code;
    this.desc = desc;
    this.abbrev = abbrev;
    this.units = units;
    this.datum = datum;
    this.isPositiveUp = isPositiveUp;
    this.isLayer = isLayer;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  public String getAbbrev() {
    return abbrev;
  }

  public String getUnits() {
    return units;
  }

  public String getDatum() {
    return datum;
  }

  public boolean isPositiveUp() {
    return isPositiveUp;
  }

  @Override
  public boolean isVerticalCoordinate() {
    return getUnits() != null;
  }

  public boolean isLayer() {
    return isLayer;
  }
}
