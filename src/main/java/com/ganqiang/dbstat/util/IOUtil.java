package com.ganqiang.dbstat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.log4j.Logger;

public final class IOUtil {


	
	
	public static void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	public static void close(InputStream in, Logger log) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				log.error(e);

			}
		}
	}

	public static void close(InputStream in, Logger log, String message) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				log.error(message, e);

			}
		}
	}
	
	public static void close(InputStream in, OutputStream out, Logger log, String message) {
    if (in != null) {
      try {
        in.close();
      } catch (IOException e) {
        log.error(message, e);
      }
    }
    if (out != null) {
      try {
        out.close();
      } catch (Exception e) {
        log.error(message, e);
      }
    }
  }
	
	public static void close(Writer writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
	public static void close(Reader in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	public static void close(Reader in, Logger log) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				log.error(e);

			}
		}
	}

}
