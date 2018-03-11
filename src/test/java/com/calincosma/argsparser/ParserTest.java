package com.calincosma.argsparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
		Assertions.assertEquals(new double[] {9.345d, -58.31d, 12.91d}, myArgs.getArray());
	}
	
	
//	@Test
//	void parsePositions() {
//		String[] params = new String[] {"-m", "This_is_mandatory", "101"};
//		MyArgs myArgs = parser.parse(params, MyArgs.class);
//		Assertions.assertEquals(101, myArgs.getPos1());
//	}
	
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
	
	@Arg("-e")
	private MyEnum aNumeration;
	
	@Arg("-f")
	private float f;
	
	@Arg("-i")
	private Integer i;
	
	@Arg("-l")
	private Long l;
	
	@Arg(value = "-m", required = true)
	private String mandatory;
	
	@Arg("-n")
	private int negative;
	
	@Arg("-o")
	private String optionalString;
	
	@Arg("-p")
	private long primitive;
	
	@Arg("-s")
	private String escapeCharacter;
	
	@Arg(value = "-set")
	private Set<Integer> set;
	
	@Arg(value = "-ts")
	private TreeSet<Long> ts;
	
	@Arg(position = 1)
	private String pos1;
	
	@Arg(position = 2)
	private int pos2;
	
	@Arg(position = 3)
	private int[] pos3;
	
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
	
	public MyEnum getaNumeration() {
		return aNumeration;
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
	
	public String getPos1() {
		return pos1;
	}
	
	public int getPos2() {
		return pos2;
	}
	
	public int[] getPos3() {
		return pos3;
	}
	
	public String getZ() {
		return z;
	}
}

enum MyEnum {
	FIRST, LAST
}