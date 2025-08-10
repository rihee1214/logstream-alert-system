package com.rihee.alerting.common.identity;

import com.rihee.alerting.common.util.StringUtils;
import java.util.UUID;
import java.util.regex.Pattern;

public final class LogKey {

  private static final String LOG_VERSION = "v1";
  private static final String UUID_VERSION = "v4";

  private static final Pattern SAFE = Pattern.compile("[^a-z0-9._-]");
  // v1:service/host/container@<uuid>-v4
  private static final String FORMAT = "%s:%s/%s/%s@%s:%s";

  // 간단 유효성 체크 (정규식+버전 접두)
  private static final Pattern KEY_PATTERN =
      Pattern.compile("^v1:[a-z0-9._-]+/[a-z0-9._-]+/[a-z0-9._-]+@[0-9a-f-]{36}-v4$");

  private LogKey() {
  }

  public static String generate(String service, String host, String container) {
    String s = norm(StringUtils.requireNonBlank(service));
    String h = norm(StringUtils.requireNonBlank(host));
    String c = norm(StringUtils.requireNonBlank(container));
    String uuid = UUID.randomUUID().toString(); // v4

    return String.format(FORMAT,
        LOG_VERSION, s, h, c, uuid, UUID_VERSION);
  }

  // --- optional: 검증/파싱 유틸 ---

  private static String norm(String v) {
    String s = v.trim().toLowerCase().replaceAll("\\s+", "-");
    return SAFE.matcher(s).replaceAll("-");
  }

  public static boolean isValid(String key) {
    return key != null && KEY_PATTERN.matcher(key).matches();
  }

//  public static String generate(String service, String host, String container) {
//
//    return String.format(FORMAT,
//                        LOG_VERSION,
//                        service,
//                        host,
//                        container,
//                        UUID_VERSION,
//                        UUID.randomUUID());
//  }

}
