package com.jule.domino.log.servlet;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/log")
public interface ILogAPI {
	@GET
	@Path(value = "/sendLog")
	@Produces({ MediaType.TEXT_PLAIN })
	public int sendLog(@QueryParam("serverID") int serverID,
                        @QueryParam("clazzName") String clazzName,
                        @QueryParam("jsonText") String jsonText );


	@GET
	@Path(value = "/submitAllLog")
	@Produces({ MediaType.TEXT_PLAIN })
	public int submitAllLog();
}
