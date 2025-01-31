package jp.microad.compassandroidsdk.util;

import jp.microad.compassandroidsdk.model.KvSet;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HtmlMacroReplacerTest {

    private HtmlMacroReplacer replacer;
    private String testHtml;

    @Before
    public void setUp() {
        replacer = new HtmlMacroReplacer();

        testHtml = "<html>" +
                "<head><title>Test</title></head>" +
                "<body>" +
                "<div id=\"${COMPASS_SPOT}\"></div>" +
                "<script>" +
                "var ifa = \"${COMPASS_EXT_IFA}\";" +
                "var appId = \"${COMPASS_EXT_APPID}\";" +
                "var gender = \"${COMPASS_EXT_GENDER}\";" +
                "var birthday = \"${COMPASS_EXT_BIRTHDAY}\";" +
                "var age = \"${COMPASS_EXT_AGE}\";" +
                "var postalCode = \"${COMPASS_EXT_POSTCODE}\";" +
                "var email = \"${COMPASS_EXT_EMAIL}\";" +
                "var hashedEmail = \"${COMPASS_EXT_HASHED_EMAIL}\";" +
                "</script>" +
                "</body></html>";
    }

    @Test
    public void testReplace_allMacrosAreReplaced() {
        String spot = "testSpot";
        String ifa = "testIFA";
        String appId = "testAppID";
        KvSet kvSet = new KvSet("M", "19900101", "34", "1234567", "test@example.com", "hashedTest");

        String resultHtml = replacer.replace(testHtml, spot, ifa, appId, kvSet);

        assertTrue(resultHtml.contains("id=\"testSpot\""));
        assertTrue(resultHtml.contains("var ifa = \"testIFA\";"));
        assertTrue(resultHtml.contains("var appId = \"testAppID\";"));
        assertTrue(resultHtml.contains("var gender = \"M\";"));
        assertTrue(resultHtml.contains("var birthday = \"19900101\";"));
        assertTrue(resultHtml.contains("var age = \"34\";"));
        assertTrue(resultHtml.contains("var postalCode = \"1234567\";"));
        assertTrue(resultHtml.contains("var email = \"test@example.com\";"));
        assertTrue(resultHtml.contains("var hashedEmail = \"hashedTest\";"));
    }

    @Test
    public void testReplace_nullValuesAreHandled() {
        String spot = "testSpot";
        String ifa = "testIFA";
        String appId = "testAppID";
        KvSet kvSet = new KvSet(null, null, null, null, null, null);

        String resultHtml = replacer.replace(testHtml, spot, ifa, appId, kvSet);

        assertTrue(resultHtml.contains("id=\"testSpot\""));
        assertTrue(resultHtml.contains("var ifa = \"testIFA\";"));
        assertTrue(resultHtml.contains("var appId = \"testAppID\";"));
        assertTrue(resultHtml.contains("var gender = \"\";"));
        assertTrue(resultHtml.contains("var birthday = \"\";"));
        assertTrue(resultHtml.contains("var age = \"\";"));
        assertTrue(resultHtml.contains("var postalCode = \"\";"));
        assertTrue(resultHtml.contains("var email = \"\";"));
        assertTrue(resultHtml.contains("var hashedEmail = \"\";"));
    }

    @Test
    public void testReplace_emptyHtmlReturnsEmpty() {
        String resultHtml = replacer.replace("", "spot", "ifa", "appId", new KvSet("M", "19900101", "34", "1234567", "test@example.com", "hashedTest"));
        assertEquals("", resultHtml);
    }

    @Test
    public void testReplace_nullHtmlReturnsEmpty() {
        String resultHtml = replacer.replace(null, "spot", "ifa", "appId", new KvSet("M", "19900101", "34", "1234567", "test@example.com", "hashedTest"));
        assertEquals("", resultHtml);
    }
}
