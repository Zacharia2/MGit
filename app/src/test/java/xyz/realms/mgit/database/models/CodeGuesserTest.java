package xyz.realms.mgit.database.models;

import static org.junit.Assert.assertEquals;

import xyz.realms.mgit.ui.utils.CodeGuesser;

/**
 *
 */
public class CodeGuesserTest {

    @org.junit.Test
    public void testGuessCodeType() throws Exception {
        assertEquals("expect to recognise java files", "text/x-java", CodeGuesser.guessCodeType("test.java"));
        assertEquals("expect to recognise typescript files", "text/typescript", CodeGuesser.guessCodeType("test.ts"));
        assertEquals("expect to recognise dart files", "text/x-dart", CodeGuesser.guessCodeType("test.dart"));
    }
}
