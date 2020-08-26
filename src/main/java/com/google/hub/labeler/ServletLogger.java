package com.google.hub.labeler;

import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload.StringPayload;
import com.google.cloud.logging.Severity;
import java.util.Collections;

class ServletLogger {

  //private static final Logger logger = Logger.getLogger(CategoryArticleRetrieval.class.getName());
  private static final Logging logger = LoggingOptions.getDefaultInstance().getService();

  public static void logText(String text) {
    LogEntry entry = LogEntry.newBuilder(StringPayload.of(text))
        .setSeverity(Severity.INFO)
        .setLogName("LOG")
        .setResource(MonitoredResource.newBuilder("global").build())
        .build();

    // Writes the log entry asynchronously
    logger.write(Collections.singleton(entry));
  }

}