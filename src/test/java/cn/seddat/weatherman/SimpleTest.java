/**
 * 
 */
package cn.seddat.weatherman;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.test.AndroidTestCase;

/**
 * @since 2012-5-18
 * @author gmz
 * 
 */
public class SimpleTest extends AndroidTestCase {

	public void testDateFormat() throws Exception {
		String date = new SimpleDateFormat("MM.dd a").format(new Date());
		System.out.println(date);
	}

}
