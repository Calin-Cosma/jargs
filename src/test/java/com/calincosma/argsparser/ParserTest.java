package com.calincosma.argsparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

class ParserTest {
	
	Parser parser = Parser.getInstance();
	
	@Test
	void parseBoolean() {
		String[] params = new String[] {"-b", "true", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(true, myArgs.isBool());
	}
	
	@Test
	void parseDouble() {
		String[] params = new String[] {"-d", "9123.34567", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(Double.valueOf("9123.34567"), myArgs.getD());
	}
	
	@Test
	void parseFloat() {
		String[] params = new String[] {"-f", "0.55", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals((float)0.55, myArgs.getF());
	}
	
	@Test
	void parseInteger() {
		String[] params = new String[] {"-i", "9876", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(Integer.valueOf(9876), myArgs.getI());
	}
	
	@Test
	void parseLong() {
		String[] params = new String[] {"-l", "847592", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(Long.valueOf(847592), myArgs.getL());
	}
	
	@Test
	void parseString() {
		String[] params = new String[] {"-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals("This_is_mandatory", myArgs.getMandatory());
	}
	
	@Test
	void parseNegative() {
		String[] params = new String[] {"-n", "-794590001", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(-794590001, myArgs.getNegative());
	}
	
	@Test
	void parseMissingRequiredParameter() {
		Assertions.assertThrows(ArgsParserException.class, () -> {
			String[] params = new String[] {"-o", "some_text"};
			MyArgs myArgs = parser.parse(params, MyArgs.class);
		});
	}
	
	@Test
	void parseIntPrimitive() {
		String[] params = new String[] {"-p", "321456", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(321456L, myArgs.getPrimitive());
	}
	
	@Test
	void parseStringList() {
		String[] params = new String[] {"-c", "First_value", "Second_value", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(Arrays.asList("First_value", "Second_value"), myArgs.getCollection());
	}
	
	@Test
	void parseIntegerSet() {
		String[] params = new String[] {"-set", "21", "49", "38", "49", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Set<Integer> set = new HashSet<>();
		set.addAll(Arrays.asList(21, 49, 38));
		Assertions.assertEquals(set, myArgs.getSet());
	}
	
	@Test
	void parseLongTreeSet() {
		String[] params = new String[] {"-ts", "21", "49", "38", "49", "12", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		TreeSet<Long> set = new TreeSet<>();
		set.addAll(Arrays.asList(12L, 21L, 38L, 49L));
		Assertions.assertEquals(set, myArgs.getTs());
	}
	
	
	@Test
	void parseDoubleArray() {
		String[] params = new String[] {"-a", "9.345", "-58.31", "12.91", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		TreeSet<Long> set = new TreeSet<>();
		set.addAll(Arrays.asList(12L, 21L, 38L, 49L));
		Assertions.assertArrayEquals(new double[] {9.345d, -58.31d, 12.91d}, myArgs.getArray());
	}
	
	
	@Test
	void parseMap() {
		String[] params = new String[] {"-map", "12=48", "5=21", "23=23", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Map<Integer, Long> map = new HashMap<>();
		map.put(12, 48L);
		map.put(5, 21L);
		map.put(23, 23L);
		Assertions.assertEquals(map, myArgs.getMap());
	}
	
	@Test
	void parseFile() {
		String[] params = new String[] {"-file", "testFile.txt", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(new File("testFile.txt"), myArgs.getFile());
	}
	
	@Test
	void parsePath() {
		String[] params = new String[] {"-path", "testPath.txt", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(Paths.get("testPath.txt"), myArgs.getPath());
	}
	
	@Test
	void parseEnum() {
		String[] params = new String[] {"-enum", "MID", "-m", "This_is_mandatory"};
		MyArgs myArgs = parser.parse(params, MyArgs.class);
		Assertions.assertEquals(MyEnum.MID, myArgs.getMyEnum());
	}
	
}


class MyArgs {
	
	@Arg("-a")
	private double[] array;
	
	@Arg("-b")
	private boolean bool;
	
	@Arg("-c")
	private List<String> collection;
	
	@Arg("-d")
	private Double d;
	
	@Arg(value = "-delim", delimiter = ",")
	private List<String> delimitedList;
	
	@Arg("-enum")
	private MyEnum myEnum;
	
	@Arg("-f")
	private float f;
	
	@Arg("-file")
	private File file;
	
	@Arg("-i")
	private Integer i;
	
	@Arg("-l")
	private Long l;
	
	@Arg(value = "-m", required = true)
	private String mandatory;
	
	@Arg(value = "-map")
	private Map<Integer, Long> map;
	
	@Arg("-n")
	private int negative;
	
	@Arg("-o")
	private String optionalString;
	
	@Arg("-p")
	private long primitive;
	
	@Arg("-path")
	private Path path;
	
	@Arg("-s")
	private String escapeCharacter;
	
	@Arg(value = "-set")
	private Set<Integer> set;
	
	@Arg(value = "-ts")
	private TreeSet<Long> ts;
	
	// no switch for z
	private String z;
	
	public double[] getArray() {
		return array;
	}
	
	public boolean isBool() {
		return bool;
	}
	
	public List<String> getCollection() {
		return collection;
	}
	
	public Double getD() {
		return d;
	}
	
	public List<String> getDelimitedList() {
		return delimitedList;
	}
	
	public MyEnum getMyEnum() {
		return myEnum;
	}
	
	public float getF() {
		return f;
	}
	
	public Integer getI() {
		return i;
	}
	
	public Long getL() {
		return l;
	}
	
	public String getMandatory() {
		return mandatory;
	}
	
	public int getNegative() {
		return negative;
	}
	
	public String getOptionalString() {
		return optionalString;
	}
	
	public long getPrimitive() {
		return primitive;
	}
	
	public String getEscapeCharacter() {
		return escapeCharacter;
	}
	
	public Set<Integer> getSet() {
		return set;
	}
	
	public TreeSet<Long> getTs() {
		return ts;
	}
	
	public String getZ() {
		return z;
	}
	
	public Map<Integer, Long> getMap() {
		return map;
	}
	
	public File getFile() {
		return file;
	}
	
	public Path getPath() {
		return path;
	}
}

enum MyEnum {
	FIRST, MID, LAST
}