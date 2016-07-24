package ucar.nc2.constants;

import java.nio.charset.Charset;

public abstract interface CDM {
	public static final String UTF8 = "UTF-8";
	public static final Charset utf8Charset = Charset.forName("UTF-8");
	public static final String CHUNK_SIZES = "_ChunkSizes";
	public static final String COMPRESS = "_Compress";
	public static final String COMPRESS_DEFLATE = "deflate";
	public static final String FIELD_ATTS = "_field_atts";
	public static final String ABBREV = "abbreviation";
	public static final String ADD_OFFSET = "add_offset";
	public static final String CONVENTIONS = "Conventions";
	public static final String DESCRIPTION = "description";
	public static final String FILL_VALUE = "_FillValue";
	public static final String HISTORY = "history";
	public static final String LONG_NAME = "long_name";
	public static final String MISSING_VALUE = "missing_value";
	public static final String SCALE_FACTOR = "scale_factor";
	public static final String TITLE = "title";
	public static final String UNITS = "units";
	public static final String UNSIGNED = "_Unsigned";
	public static final String VALID_RANGE = "valid_range";
	public static final String ARAKAWA_E = "Arakawa-E";
	public static final String CF_EXTENDED = "CDM-Extended-CF";
	public static final String FILE_FORMAT = "file_format";
	public static final String LAT_UNITS = "degrees_north";
	public static final String LON_UNITS = "degrees_east";
	public static final String TIME_INTERVAL = "time_interval";
}
