// Copyright Kevin D.Hall 2014-2018

package com.khallware.api;

import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.servlet.ServletException;
import nl.captcha.servlet.CaptchaServletUtil;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;
import nl.captcha.text.renderer.ColoredEdgesWordRenderer;
import nl.captcha.Captcha.Builder;
import nl.captcha.Captcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * ServletContainer.  The "Main" entry point for the web application.
 * This is where the captcha thread is started.
 *
 * @author khall
 */
public class ServletContainer
		extends org.glassfish.jersey.servlet.ServletContainer
{
	private static final Logger logger = LoggerFactory.getLogger(
		ServletContainer.class);
	public static final String PROP_CAPTCHA_FILE = "captcha_file";
	public static final String PROP_CAPTCHA_REFRESH = "captcha_refresh";
	public static final String PROP_CAPTCHA_WIDTH = "captcha_width";
	public static final String PROP_CAPTCHA_HEIGHT = "captcha_height";
	public static final String PROP_CFG = "main_cfg";
	public static final String DEF_CAPTCHA_FILE = "/tmp/captcha.jpg";
	public static final String INTPROPFILE = "apis.properties";
	public static final String EXTPROPFILE = "/tmp/apis.properties";
	public static final long DEF_CAPTCHA_REFRESH = (5 * 60 * 1000);
	public static final int DEF_WIDTH = 200;
	public static final int DEF_HEIGHT = 50;
	private static Captcha captcha = null;

	public ServletContainer()
	{
		super();
	}

	public ServletContainer(ResourceConfig resourceConfig)
	{
		super(resourceConfig);
	}

	@Override
	public void init() throws ServletException
	{
		ClassLoader cl = null;
		try {
			super.init();
			cl = Thread.currentThread().getContextClassLoader();
			InputStream is = cl.getResourceAsStream(INTPROPFILE);
			Properties props = new Properties();
			props.load(is);
			String fname = props.getProperty(PROP_CFG, EXTPROPFILE);

			if (new File(fname).exists()) {
				props.load(new FileInputStream(fname));
			}
			logger.trace(""+props);
			Datastore.DS().configure(props);
			startCaptchaThread();
			logger.info("configured servlet container");

			if (!Datastore.DS().ping()) {
				logger.error("Failed to connect to database!");
				System.exit(1);
			}
		}
		catch (SecurityException|IOException e) {
			logger.error(""+e, e);
		}
	}

	public static Captcha getCaptcha()
	{
		if (captcha == null) {
			captcha = makeCaptcha();
		}
		return(captcha);
	}

	protected void startCaptchaThread()
	{
		logger.info("starting captcha thread");
		new Thread(() -> {
			while (true) {
				writeCaptchaImageThenSleep();
			}
		}).start();
	}

	protected void writeCaptchaImageThenSleep()
	{
		File file = new File(Datastore.DS().getProperty(
			PROP_CAPTCHA_FILE, DEF_CAPTCHA_FILE));
		long sleeptime = DEF_CAPTCHA_REFRESH;
		try {
			String val = Datastore.DS().getProperty(
				PROP_CAPTCHA_REFRESH, ""+sleeptime);
			sleeptime = Long.parseLong(val);
		}
		catch (NumberFormatException e) {
			logger.error(""+e, e);
		}
		writeCaptchaImageThenSleep(file, sleeptime);
	}

	protected void writeCaptchaImageThenSleep(File file, long sleeptime)
	{
		try {
			file.getParentFile().mkdirs();
			CaptchaServletUtil.writeImage(
				new FileOutputStream(file),
				getCaptcha().getImage());
		}
		catch (IOException e1) {
			logger.error(""+e1, e1);
		}
		finally {
			try {
				Thread.currentThread().sleep(sleeptime);
			}
			catch (InterruptedException e2) {
				logger.error(""+e2, e2);
			}
		}
	}

	public static Captcha makeCaptcha()
	{
		Captcha retval = null;
		int width = DEF_WIDTH;
		int height = DEF_HEIGHT;
		List<Font> fontList = new ArrayList<>(3);
		List<Color> colorList = new ArrayList<>(2);
		GradiatedBackgroundProducer backgroundProducer =
			new GradiatedBackgroundProducer();
		ColoredEdgesWordRenderer wordRenderer = null;
		logger.trace("getting width and height properties");
		try {
			String val = Datastore.DS().getProperty(
				PROP_CAPTCHA_WIDTH, ""+width);
			width = Integer.parseInt(val);
			val = Datastore.DS().getProperty(
				PROP_CAPTCHA_HEIGHT, ""+height);
			height = Integer.parseInt(val);
			logger.trace("width = {} and height = {}",width,height);
		}
		catch (NumberFormatException e) {
			logger.error(""+e, e);
		}
		colorList.add(Color.BLACK);
		colorList.add(Color.BLUE);
		fontList.add(new Font("Geneva", Font.ITALIC, 48));
		fontList.add(new Font("Courier", Font.BOLD, 48));
		fontList.add(new Font("Arial", Font.BOLD, 48));
		wordRenderer = new ColoredEdgesWordRenderer(colorList,
			fontList);
		logger.trace("building captcha instance");
		retval = new Builder(width, height)
			.addText(wordRenderer)
			// .gimp()
			.addNoise()
			.addBackground(backgroundProducer)
			.build();
		return(retval);
	}
}
