package edu.mum.waa;


import java.io.*;


// Extend HttpServlet class
public class contacts  {
 


   public void doGet(BBHttpRequest httpRequest, BBHttpResponse httpResponse)
    
   {
   
   // Set response content type
   httpResponse.setContentType("text/html");

   
   StringBuilder response = new StringBuilder();
	response.append("<!DOCTYPE html>");
	response.append("<html>");
	response.append("<head>");
	response.append("<title>Almost an HTTP Server</title>");
	response.append("</head>");
	response.append("<body>");
	response.append("<h1>This is the HTTP Server</h1>");
	response.append("<h2>This is contacts generator</h2>\r\n");
	response.append(Math.random());
	response.append("</body>");
	response.append("</html>");

	httpResponse.setStatusCode(200);
	httpResponse.setMessage(response.toString());
  
   }

   
}



