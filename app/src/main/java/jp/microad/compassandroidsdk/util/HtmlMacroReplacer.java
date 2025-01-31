package jp.microad.compassandroidsdk.util;

import jp.microad.compassandroidsdk.model.KvSet;

public class HtmlMacroReplacer {
    private static final String SPOT_MACRO = "${COMPASS_SPOT}";
    private static final String IFA_MACRO = "${COMPASS_EXT_IFA}";
    private static final String APPID_MACRO = "${COMPASS_EXT_APPID}";
    private static final String GENDER_MACRO = "${COMPASS_EXT_GENDER}";
    private static final String BIRTHDAY_MACRO = "${COMPASS_EXT_BIRTHDAY}";
    private static final String AGE_MACRO = "${COMPASS_EXT_AGE}";
    private static final String POSTCODE_MACRO = "${COMPASS_EXT_POSTCODE}";
    private static final String EMAIL_MACRO = "${COMPASS_EXT_EMAIL}";
    private static final String HASHED_EMAIL_MACRO = "${COMPASS_EXT_HASHED_EMAIL}";

    public String replace(String html, String spot, String ifa, String appId, KvSet kvSet) {
        if (html == null) return "";
        return html
                .replace(SPOT_MACRO, spot)
                .replace(IFA_MACRO, ifa)
                .replace(APPID_MACRO, appId)
                .replace(GENDER_MACRO, kvSet.getGender() != null ? kvSet.getGender() : "")
                .replace(BIRTHDAY_MACRO, kvSet.getBirthday() != null ? kvSet.getBirthday() : "")
                .replace(AGE_MACRO, kvSet.getAge() != null ? kvSet.getAge() : "")
                .replace(POSTCODE_MACRO, kvSet.getPostalCode() != null ? kvSet.getPostalCode() : "")
                .replace(EMAIL_MACRO, kvSet.getEmail() != null ? kvSet.getEmail() : "")
                .replace(HASHED_EMAIL_MACRO, kvSet.getHashedEmail() != null ? kvSet.getHashedEmail() : "");
    }
}
