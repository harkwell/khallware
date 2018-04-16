package com.khallware.api;

import com.khallware.api.ctrl.*;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import java.util.HashSet;
import java.util.Set;

public class Main
{
	public static final String DEF_URL = "http://localhost:80";

	private static Set<Class<?>> getRestClasses()
	{
		Set<Class<?>> retval = new HashSet<>();
		retval.add(Admin.class);
		retval.add(Atoms.class);
		retval.add(Blogs.class);
		retval.add(Bookmarks.class);
		retval.add(Comments.class);
		retval.add(Contacts.class);
		retval.add(DefaultHandler.class);
		retval.add(Events.class);
		retval.add(FileItems.class);
		retval.add(Locations.class);
		retval.add(Photos.class);
		retval.add(Search.class);
		retval.add(Security.class);
		retval.add(Sounds.class);
		retval.add(StaticContent.class);
		retval.add(Tags.class);
		// retval.add(Upload.class);
		retval.add(Videos.class);
		return(retval);
	}

	public static void main(String... args) throws Exception
	{
		String url = (args.length > 0) ? args[0] : DEF_URL;
		ResourceConfig cfg = new PackagesResourceConfig("apis") {
			public Set<Class<?>> getClasses()
			{
				return(Main.getRestClasses());
			}
		};
		HttpServer svr = GrizzlyServerFactory.createHttpServer(url,cfg);
		svr.start();
		System.out.println("press enter to stop...");
		System.in.read();
	}
}
