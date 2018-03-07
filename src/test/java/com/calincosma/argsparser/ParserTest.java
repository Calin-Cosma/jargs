package com.calincosma.argsparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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
	
	@Arg(value = "-t", delimiter = "|")
	private Set<Integer> tabs;
	
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
	
	public Set<Integer> getTabs() {
		return tabs;
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