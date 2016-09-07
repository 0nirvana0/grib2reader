package grib.test;

import java.io.IOException;
import java.util.Date;

import ucar.nc2.grib.grib2.Grib2Gds;
import ucar.nc2.grib.grib2.Grib2Gds.LatLon;
import ucar.nc2.grib.grib2.Grib2Pds;
import ucar.nc2.grib.grib2.Grib2Record;
import ucar.nc2.grib.grib2.Grib2RecordScanner;
import ucar.nc2.grib.grib2.Grib2SectionBitMap;
import ucar.nc2.grib.grib2.Grib2SectionData;
import ucar.nc2.grib.grib2.Grib2SectionDataRepresentation;
import ucar.nc2.grib.grib2.Grib2SectionGridDefinition;
import ucar.nc2.grib.grib2.Grib2SectionIdentification;
import ucar.nc2.grib.grib2.Grib2SectionIndicator;
import ucar.nc2.grib.grib2.Grib2SectionLocalUse;
import ucar.nc2.grib.grib2.Grib2SectionProductDefinition;
import ucar.nc2.grib.grib2.table.Grib2Customizer;
import ucar.unidata.io.RandomAccessFile;

public class Grib2Test {
	/**
	 * Method description goes here.
	 * @param args
	 */
	public static void main(String[] args) {
		//String path = "E:/data/Z_NWGD_C_BABJ_20160729232241_P_RFFC_SCMOC-ER03_201607300800_24003.GRB2";
		 String path = "E:/data/EN_GRAB2/20160731/Z_NWGD_C_BABJ_20160730203316_P_RFFC_SCMOC-TMAX_201607310800_24024.GRB2";
		TestReadGrab2(path);
	}

	public static void TestReadGrab2(String path) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path, "r");
			Grib2RecordScanner scan = new Grib2RecordScanner(raf);
			int i = 0;
			while (scan.hasNext()) {
				Grib2Record gr2 = scan.next();
				// do stuff
				// section 0 指示段 包含GRIB、学科、GRIB 码版本号、资料长度
				Grib2SectionIndicator iss = gr2.getIs();

				StringBuffer section0 = new StringBuffer();
				section0.append("section0 : ");
				section0.append("StartingPosition ").append(iss.getStartPos()).append(",");
				section0.append("MessageLength ").append(iss.getMessageLength()).append(",");
				section0.append("EndPos ").append(iss.getEndPos()).append(",");

				// section 1 标识段 包含段长、段号,应用于GRIB 资料中全部加工数据的特征---时间
				Grib2SectionIdentification ids = gr2.getId();
				StringBuilder section1 = new StringBuilder();
				Date referenceDate = ids.getReferenceDate();

				section1.append("section1 : ").append(ids.toString()).append(",");
				section1.append("referenceDate : ").append(referenceDate.toString());

				// section 2 本地使用段 包含段长、段号,由编报中心附加的本地使用的信息
				Grib2SectionLocalUse lus = gr2.getLocalUseSection();
				StringBuilder section2 = new StringBuilder();
				byte[] lu = lus.getRawBytes();
				section2.append("section2 : ");
				if (lu != null && lu.length != 0) {
					section2.append("Length ").append(lu.length).append(",");
					section2.append("str ").append(new String(lu)).append(",");
				} else {
					section2.append("Length 0,");
				}
				// section 3 网格定义段 包含段长、段号、网格面和面内数据的几何形状定义
				Grib2SectionGridDefinition gds = gr2.getGDSsection();

				Grib2Gds tempGds = gds.getGDS();
				tempGds.getNxRaw();// 每行格数
				tempGds.getNyRaw();// 行数
				StringBuilder section3 = new StringBuilder();
				section3.append("section3 : ");
				section3.append("Length ").append(gds.getLength()).append(",");
				section3.append("NumberPoints ").append(tempGds.getNyRaw() + "*" + tempGds.getNxRaw() + "=" + gds.getNumberPoints()).append(",");
				String gdsStr = null;
				if (tempGds.isLatLon()) {
					// 经纬度范围
					LatLon ll = (LatLon) tempGds;
					String la = "la:" + ll.la1 + "~" + ll.la2 + ",deltaLat:" + ll.deltaLat + ",";
					String lo = "lo:" + ll.lo1 + "~" + ll.lo2 + ",deltaLon:" + ll.deltaLon + ",";
					gdsStr = la + lo;
				}
				section3.append(gdsStr);

				// section 4 产品定义段 包括段长、段号、数据的性质描述
				Grib2SectionProductDefinition pds = gr2.getPDSsection();
				int PDSTemplateNumber = pds.getPDSTemplateNumber();
				Grib2Pds tempPds = pds.getPDS();

				int forecastTime = tempPds.getForecastTime();

				// 参数层高由以下四个数值决定
				int type1 = tempPds.getLevelType1();
				float value1 = (float) tempPds.getLevelValue1();
				int type2 = tempPds.getLevelType2();
				double value2 = tempPds.getLevelValue2();
				
				 Grib2Customizer gc2 = Grib2Customizer.factory(gr2);
				// 从xml中读取层高类型
				// String type1str = gc2.getLevelName(type1);
				// String type2str = gc2.getLevelName(type2);
				//
				// VertUnit vertUnit = gc2.getVertUnit(type1);
				// String units = vertUnit.getUnits();
				// String s = type1str + ":" + value1 + "(" + units + ") ";
				// if (!"Missing".equalsIgnoreCase(type2str)) {
				// s += "-" + value2;
				// }

				// 参数类型由以下三个参数决定
				int d = iss.getDiscipline();
				int c = tempPds.getParameterCategory();
				int n = tempPds.getParameterNumber();

				int paramType = -1;// 0温度，1相对湿度，2降水,3风向,4风速U分量，5风速V分量，6气压,
				if (d == 0 && c == 0 && n == 0) {
					// 温度
					paramType = 0;
				}
				if (d == 0 && c == 1 && n == 1) {
					// 相对湿度
					paramType = 1;
				}
				if (d == 0 && c == 1 && n == 8) {
					// 降水
					paramType = 2;
				}
				if (d == 0 & c == 2 && n == 0) {
					// 风向
					paramType = 3;
				}
				if (d == 0 & c == 2 && n == 2) {
					// 风速U风量
					paramType = 4;
				}
				if (d == 0 & c == 2 && n == 3) {
					// 风速V风量
					paramType = 5;
				}
				if (d == 0 && c == 3 && n == 0) {
					// 气压
					paramType = 6;
				}

				// 从xml中读取参数类型
				// Parameter param = gc2.getParameter(d, c, n);
				// if (param == null) {
				// if (paramType != -1) {
				// System.out.println("error");
				// }
				// continue;
				// }

				StringBuilder section4 = new StringBuilder();
				section4.append("section4 : ");
				section4.append("Length:").append(pds.getLength()).append(",");
				section4.append("Length:").append("PDSTemplateNumber:" + PDSTemplateNumber).append(",");
				section4.append("d c n:").append(d + " " + c + " " + n).append(",");
				// section4.append("name:").append(param.getName()).append(",");
				// section4.append("unit:").append(param.getUnit()).append(",");
				// section4.append("abbrev:").append(param.getAbbrev()).append(",");
				// section4.append(s);
				section4.append(",LevelType1:").append(type1).append(",");
				section4.append("forecastTime:" + forecastTime).append(",");

				// section 5 数据表示段 包括段长、段号、数据值表示法描述
				Grib2SectionDataRepresentation drs = gr2.getDataRepresentationSection();
				int dateTemplate = drs.getDataTemplate();
				StringBuilder section5 = new StringBuilder();
				section5.append("section5 : ");
				section5.append("DataTemplate : ").append(dateTemplate).append(",");
				section5.append("StartingPosition ").append(drs.getStartingPosition()).append(",");
				section5.append("Length ").append(drs.getLength(raf)).append(",");
				section5.append("DataPoints ").append(drs.getDataPoints()).append(",");

				// section 6 包括段长、段号,以及指示每个格点上的数据是否存在
				Grib2SectionBitMap bms = gr2.getBitmapSection();
				StringBuilder section6 = new StringBuilder();
				section6.append("section6 : ");
				section6.append("StartingPosition ").append(bms.getStartingPosition()).append(",");
				section6.append("BitMapIndicator ").append(bms.getBitMapIndicator()).append(",");

				// section 7
				Grib2SectionData bds = gr2.getDataSection();
				StringBuilder section7 = new StringBuilder();
				section7.append("section7 : ");
				section7.append("StartingPosition ").append(bds.getStartingPosition()).append(",");
				section7.append("MsgLength ").append(bds.getMsgLength()).append(",");

				if (paramType != -1) {
					float[] data = gr2.readData(raf, drs.getStartingPosition());
					// System.out.println(drs.getStartingPosition());
					// System.out.println(Arrays.toString(data));
					System.out.println(i++ + "\n" + section0.toString() + "\n" + section1.toString() + "\n" + section2.toString() + "\n" + section3.toString() + "\n" + section4.toString() + "\n" + section5.toString() + "\n" + section6.toString() + "\n" + section7.toString() + "\n");
					for (int j = 0; j < 5; j++) {
						System.out.print(data[j] + " ");
					}
					System.out.println("\n");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}
