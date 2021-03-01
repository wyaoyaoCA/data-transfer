package study.wyy.datatransfer.api.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * @author wyaoyao
 * @description
 * @date 2021/2/7 20:43
 */
public abstract class ParamUtils {

	public static void isBlank(String string, String msg) {
		if (StringUtils.isNotBlank(string)) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void notBlank(String string, String msg) {
		if (StringUtils.isBlank(string)) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void notEmpty(Collection collection, String msg) {
		if (null == collection || collection.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void nonBlankElements(Collection<String> collection, String elementMsg) {
		Iterator<String> iterator = collection.iterator();

		while (iterator.hasNext()) {
			String str = iterator.next();
			notBlank(str, elementMsg);
		}

	}

	public static void nonNull(Object object, String msg) {
		if (null == object) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void isNull(Object object, String msg) {
		if (null != object) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void expectTrue(boolean boolExpression, String falseMsg) {
		if (!boolExpression) {
			throw new IllegalArgumentException(falseMsg);
		}
	}

	public static void expectFalse(boolean boolExpression, String trueMsg) {
		if (boolExpression) {
			throw new IllegalArgumentException(trueMsg);
		}
	}

	public static void expectAnyFalse(String msg, Boolean... booleans) throws IllegalArgumentException {
		if (Arrays.stream(booleans).allMatch((t) -> {
			return t;
		})) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void expectInRange(Collection collection, int minElements, int maxElements, String msg) {
		expectInRange(collection.size(), minElements, maxElements, msg);
	}

	public static void expectInRange(String string, int minLength, int maxLength, String msg) {
		if (StringUtils.isBlank(string) || string.length() < minLength || string.length() > maxLength) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void expectInRange(int value, int minValue, int maxValue, String msg) {
		if (value < minValue || value > maxValue) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void expectDateStrWithPattern(String sDate, String pattern, String msg) {
		Date outDate = null;
		if (StringUtils.isBlank(sDate)) {
			throw new IllegalArgumentException(msg);
		} else if (!StringUtils.isBlank(pattern)) {
			SimpleDateFormat df = new SimpleDateFormat(pattern);

			try {
				df.parse(sDate);
			} catch (ParseException e) {
				throw new IllegalArgumentException(msg);
			}
		}
	}
}
