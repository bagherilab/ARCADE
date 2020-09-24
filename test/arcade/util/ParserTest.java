package arcade.util;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.Random;
import static arcade.util.Parser.*;

public class ParserTest {
	private static final String COMMAND_ID_1 = randomString();
	private static final String COMMAND_ID_2 = randomString();
	private static final String COMMAND_ID_3 = randomString();
	
	static String randomString() {
		return new Random().ints(65, 91)
				.limit(10)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}
	
	static int randomInt() { return (int)(Math.random()*10) + 2; }
	
	static String[] randomValues(int n) {
		String[] strings = new String[n];
		for (int i = 0; i < n; i++) { strings[i] = randomString(); }
		return strings;
	}
	
	@Test
	public void constructor_called_setsObjects() {
		Parser parser = new Parser(new Box());
		assertNotNull(parser.allCommands);
		assertNotNull(parser.shortToCommand);
		assertNotNull(parser.longToCommand);
		assertNotNull(parser.positionCommands);
	}
	
	@Test
	public void constructor_emptyTags_addsCommands() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		box.addTag(COMMAND_ID_2, "OPTION");
		box.addTag(COMMAND_ID_3, "SWITCH");
		
		Parser parser = new Parser(box);
		
		assertEquals(3, parser.allCommands.size());
		assertEquals(1, parser.positionCommands.size());
		assertEquals(0, parser.shortToCommand.size());
		assertEquals(0, parser.longToCommand.size());
	}
	
	@Test
	public void constructor_withFlags_addsCommands() {
		String longFlag = randomString();
		String shortFlag = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_2, "OPTION");
		box.addTag(COMMAND_ID_3, "SWITCH");
		box.addAtt(COMMAND_ID_2, "long", longFlag);
		box.addAtt(COMMAND_ID_3, "short", shortFlag);
		
		Parser parser = new Parser(box);
		
		assertEquals(2, parser.allCommands.size());
		assertEquals(0, parser.positionCommands.size());
		assertEquals(1, parser.shortToCommand.size());
		assertEquals(1, parser.longToCommand.size());
	}
	
	@Test
	public void constructor_emptyTags_assignsTypes() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		box.addTag(COMMAND_ID_2, "OPTION");
		box.addTag(COMMAND_ID_3, "SWITCH");
		
		Parser parser = new Parser(box);
		
		assertEquals(POSITION, parser.allCommands.get(0).type);
		assertEquals(OPTION, parser.allCommands.get(1).type);
		assertEquals(SWITCH, parser.allCommands.get(2).type);
	}
	
	@Test
	public void constructor_givenHelp_assignsField() {
		String helpText1 = randomString();
		String helpText2 = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		box.addTag(COMMAND_ID_2, "OPTION");
		box.addTag(COMMAND_ID_3, "SWITCH");
		box.addAtt(COMMAND_ID_1, "help", helpText1);
		box.addAtt(COMMAND_ID_3, "help", helpText2);
		
		Parser parser = new Parser(box);
		
		assertEquals(helpText1, parser.allCommands.get(0).help);
		assertEquals(helpText2, parser.allCommands.get(2).help);
		assertNull(parser.allCommands.get(1).help);
	}
	
	@Test
	public void constructor_givenDefaultOption_assignsField() {
		String defaultValue1 = randomString();
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		box.addAtt(COMMAND_ID_1, "default", defaultValue1);
		Parser parser = new Parser(box);
		assertEquals(defaultValue1, parser.allCommands.get(0).defaults);
	}
	
	@Test
	public void constructor_givenDefaultSwitch_doesNothing() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "SWITCH");
		box.addAtt(COMMAND_ID_1, "default", randomString());
		Parser parser = new Parser(box);
		assertNull(parser.allCommands.get(0).defaults);
	}
	
	@Test
	public void constructor_givenDefaultPosition_doesNothing() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		box.addAtt(COMMAND_ID_1, "default", randomString());
		Parser parser = new Parser(box);
		assertNull(parser.allCommands.get(0).defaults);
	}
	
	@Test
	public void constructor_givenShortFlagNotPosition_assignsField() {
		String shortFlag = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		box.addTag(COMMAND_ID_2, "SWITCH");
		box.addAtt(COMMAND_ID_1, "short", shortFlag);
		
		Parser parser = new Parser(box);
		
		assertEquals(shortFlag, parser.allCommands.get(0).shortFlag);
		assertNull(parser.allCommands.get(1).shortFlag);
	}
	
	@Test
	public void constructor_givenLongFlagNotPosition_assignsField() {
		String longFlag = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		box.addTag(COMMAND_ID_2, "SWITCH");
		box.addAtt(COMMAND_ID_1, "long", longFlag);
		
		Parser parser = new Parser(box);
		
		assertEquals(longFlag, parser.allCommands.get(0).longFlag);
		assertNull(parser.allCommands.get(1).longFlag);
	}
	
	@Test
	public void constructor_givenBothFlagsNotPosition_assignsField() {
		String shortFlag = randomString();
		String longFlag = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		box.addTag(COMMAND_ID_2, "SWITCH");
		box.addAtt(COMMAND_ID_2, "short", shortFlag);
		box.addAtt(COMMAND_ID_2, "long", longFlag);
		
		Parser parser = new Parser(box);
		
		assertEquals(shortFlag, parser.allCommands.get(1).shortFlag);
		assertEquals(longFlag, parser.allCommands.get(1).longFlag);
		assertNull(parser.allCommands.get(0).shortFlag);
		assertNull(parser.allCommands.get(0).longFlag);
	}
	
	@Test
	public void constructor_givenBothFlagsPosition_doesNothing() {
		String shortFlag = randomString();
		String longFlag = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		box.addAtt(COMMAND_ID_1, "short", shortFlag);
		box.addAtt(COMMAND_ID_1, "long", longFlag);
		
		Parser parser = new Parser(box);
		
		assertNull(parser.allCommands.get(0).shortFlag);
		assertNull(parser.allCommands.get(0).longFlag);
	}
	
	@Test
	public void parse_noConfigNoArgs_returnsEmpty() {
		Box box = new Box();
		Parser parser = new Parser(box);
		MiniBox parsed = parser.parse(new String[] {});
		assertTrue(parsed.compare(new MiniBox()));
	}
	
	@Test
	public void parse_withoutPositionNoArgs_returnsEmpty() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "SWITCH");
		
		Parser parser = new Parser(box);
		MiniBox parsed = parser.parse(new String[] {});
		assertTrue(parsed.compare(new MiniBox()));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void parse_withPositionNoArgs_throwsException() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		
		Parser parser = new Parser(box);
		MiniBox parsed = parser.parse(new String[] {});
		assertTrue(parsed.compare(new MiniBox()));
	}
	
	@Test
	public void parse_withDefaultsNoArgs_usesDefaults() {
		String defaultValue = randomString();
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		box.addAtt(COMMAND_ID_1, "default", defaultValue);
		
		Parser parser = new Parser(box);
		MiniBox parsed = parser.parse(new String[] {});
		
		MiniBox expected = new MiniBox();
		expected.put(COMMAND_ID_1, defaultValue);
		
		assertTrue(parsed.compare(expected));
	}
	
	@Test
	public void parse_withArgs_updatesValues() {
		String defaultValue1 = randomString();
		String defaultValue2 = randomString();
		String newValue = randomString();
		String longFlag = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		box.addAtt(COMMAND_ID_1, "default", defaultValue1);
		box.addAtt(COMMAND_ID_1, "long", longFlag);
		box.addTag(COMMAND_ID_2, "OPTION");
		box.addAtt(COMMAND_ID_2, "default", defaultValue2);
		
		Parser parser = new Parser(box);
		MiniBox parsed = parser.parse(new String[] {
				"--" + longFlag,
				newValue
		});
		
		MiniBox expected = new MiniBox();
		expected.put(COMMAND_ID_1, newValue);
		expected.put(COMMAND_ID_2, defaultValue2);
		
		assertTrue(parsed.compare(expected));
	}
	
	@Test
	public void addDefaults_onlyPositions_doesNothing() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		box.addTag(COMMAND_ID_2, "POSITION");
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		
		parser.addDefaults();
		assertTrue(parser.parsed.compare(new MiniBox()));
	}
	
	@Test
	public void addDefaults_noDefaults_doesNothing() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "SWITCH");
		box.addTag(COMMAND_ID_2, "SWITCH");
		box.addTag(COMMAND_ID_3, "OPTION");
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		
		parser.addDefaults();
		assertTrue(parser.parsed.compare(new MiniBox()));
	}
	
	@Test
	public void addDefaults_hasDefaults_setsDefaults() {
		String defaultValue1 = randomString();
		String defaultValue2 = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		box.addTag(COMMAND_ID_2, "OPTION");
		box.addTag(COMMAND_ID_3, "OPTION");
		box.addAtt(COMMAND_ID_2, "default", defaultValue1);
		box.addAtt(COMMAND_ID_3, "default", defaultValue2);
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		parser.addDefaults();
		
		MiniBox expected = new MiniBox();
		expected.put(COMMAND_ID_2, defaultValue1);
		expected.put(COMMAND_ID_3, defaultValue2);
		
		assertTrue(parser.parsed.compare(expected));
	}
	
	@Test
	public void parseArg_givenPositions_parsesArgument() { 
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		box.addTag(COMMAND_ID_2, "POSITION");
		
		String[] arguments = new String[] {
				randomString(),
				randomString(),
		};
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		parser.parseArguments(arguments);
		
		MiniBox expected = new MiniBox();
		expected.put(COMMAND_ID_1, arguments[0]);
		expected.put(COMMAND_ID_2, arguments[1]);
		
		assertTrue(parser.parsed.compare(expected));
	}
	
	@Test
	public void parseArg_givenOptions_parsesArgument() {
		String shortFlag = randomString();
		String longFlag = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		box.addAtt(COMMAND_ID_1, "short", shortFlag);
		box.addTag(COMMAND_ID_2, "OPTION");
		box.addAtt(COMMAND_ID_2, "long", longFlag);
		
		String[] arguments = new String[] {
				"--" + longFlag,
				randomString(),
				"-" + shortFlag,
				randomString(),
		};
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		parser.parseArguments(arguments);
		
		MiniBox expected = new MiniBox();
		expected.put(COMMAND_ID_1, arguments[3]);
		expected.put(COMMAND_ID_2, arguments[1]);
		
		assertTrue(parser.parsed.compare(expected));
	}
	
	@Test
	public void parseArg_givenSwitches_parsesArgument() {
		String shortFlag = randomString();
		String longFlag = randomString();
		
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "SWITCH");
		box.addAtt(COMMAND_ID_1, "short", shortFlag);
		box.addTag(COMMAND_ID_2, "SWITCH");
		box.addAtt(COMMAND_ID_2, "long", longFlag);
		box.addTag(COMMAND_ID_3, "SWITCH");
		
		String[] arguments = new String[] {
				"--" + longFlag,
				"-" + shortFlag
		};
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		parser.parseArguments(arguments);
		
		MiniBox expected = new MiniBox();
		expected.put(COMMAND_ID_1, "");
		expected.put(COMMAND_ID_2, "");
		
		assertTrue(parser.parsed.compare(expected));
	}
	
	@Test
	public void parsePositionArgument_givenPosition_parsesArgument() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		box.addTag(COMMAND_ID_2, "POSITION");
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		parser.positionIndex = 1;
		
		int index = randomInt();
		String[] arguments = randomValues(index + randomInt());
		arguments[index] = randomString();
		parser.parsePositionArgument(arguments, index);
		
		MiniBox expected = new MiniBox();
		expected.put(COMMAND_ID_2, arguments[index]);
		
		assertTrue(parser.parsed.compare(expected));
		assertEquals(2, parser.positionIndex);
	}
	
	@Test
	public void parsePositionArgument_givenPosition_updatesIndex() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "POSITION");
		box.addTag(COMMAND_ID_2, "POSITION");
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		parser.positionIndex = 1;
		
		int index = randomInt();
		String[] arguments = randomValues(index + randomInt());
		int newIndex = parser.parsePositionArgument(arguments, index);
		
		assertEquals(index + 1, newIndex);
	}
	
	@Test
	public void parseFlaggedArgument_givenSwitch_parsesArgument() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "SWITCH");
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		
		int index = randomInt();
		String[] arguments = randomValues(index + randomInt());
		parser.parseFlaggedArgument(arguments, index, parser.allCommands.get(0));
		
		MiniBox expected = new MiniBox();
		expected.put(COMMAND_ID_1, "");
		assertTrue(parser.parsed.compare(expected));
	}
	
	@Test
	public void parseFlaggedArgument_givenSwitch_updatesIndex() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "SWITCH");
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		
		int index = randomInt();
		String[] arguments = randomValues(index + randomInt());
		int newIndex = parser.parseFlaggedArgument(arguments, index, parser.allCommands.get(0));
		
		assertEquals(index + 1, newIndex);
	}
	
	@Test
	public void parseFlaggedArgument_givenOption_parsesArgument() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		
		int index = randomInt();
		String[] arguments = randomValues(index + randomInt());
		arguments[index + 1] = arguments[index];
		parser.parseFlaggedArgument(arguments, index, parser.allCommands.get(0));
		
		MiniBox expected = new MiniBox();
		expected.put(COMMAND_ID_1, arguments[index + 1]);
		assertTrue(parser.parsed.compare(expected));
	}
	
	@Test
	public void parseFlaggedArgument_givenOption_updatesIndex() {
		Box box = new Box();
		box.addTag(COMMAND_ID_1, "OPTION");
		
		Parser parser = new Parser(box);
		parser.parsed = new MiniBox();
		
		int index = randomInt();
		String[] arguments = randomValues(index + randomInt());
		int newIndex =  parser.parseFlaggedArgument(arguments, index, parser.allCommands.get(0));
		
		assertEquals(index + 2, newIndex);
	}
}
