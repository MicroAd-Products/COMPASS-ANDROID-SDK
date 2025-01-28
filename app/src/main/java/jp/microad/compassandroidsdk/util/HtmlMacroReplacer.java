package jp.microad.compassandroidsdk.util;

import jp.microad.compassandroidsdk.model.KvSet;

public class HtmlMacroReplacer {
    public String replace(String html, String spot, String ifa, String appId, KvSet kvSet) {
        return html
                .replace("${COMPASS_SPOT}", spot)
                .replace("${COMPASS_EXT_IFA}", ifa)
                .replace("${COMPASS_EXT_APPID}", appId)
                .replace("${COMPASS_EXT_GENDER}", kvSet.getGender() != null ? kvSet.getGender() : "")
                .replace("${COMPASS_EXT_BIRTHDAY}", kvSet.getBirthday() != null ? kvSet.getBirthday() : "")
                .replace("${COMPASS_EXT_AGE}", kvSet.getAge() != null ? kvSet.getAge() : "")
                .replace("${COMPASS_EXT_POSTALCODE}", kvSet.getPostalCode() != null ? kvSet.getPostalCode() : "")
                .replace("${COMPASS_EXT_EMAIL}", kvSet.getEmail() != null ? kvSet.getEmail() : "")
                .replace("${COMPASS_EXT_HASHED_EMAIL}", kvSet.getHashedEmail() != null ? kvSet.getHashedEmail() : "");
    }
}
