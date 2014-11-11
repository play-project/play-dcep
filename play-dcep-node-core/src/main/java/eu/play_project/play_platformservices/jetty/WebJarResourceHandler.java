package eu.play_project.play_platformservices.jetty;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.WriterOutputStream;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.webjars.WebJarAssetLocator;

public class WebJarResourceHandler extends ResourceHandler {
	
	private final WebJarAssetLocator webJarAssetLocator;

	public WebJarResourceHandler() {
		super();
		this.webJarAssetLocator = new WebJarAssetLocator();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
        
        if (baseRequest.isHandled())
            return;

        boolean skipContentBody = false;

        if(!HttpMethods.GET.equals(request.getMethod()))
        {
            if(!HttpMethods.HEAD.equals(request.getMethod()))
            {
                //try another handler
                super.handle(target, baseRequest, request, response);
                return;
            }
            skipContentBody = true;
        }
        
        Resource resource = Resource.newClassPathResource(this.webJarAssetLocator.getFullPath(target));

        if (resource==null || !resource.exists())
        {
            if (target.endsWith("/jetty-dir.css"))
            {
                resource = getStylesheet();
                if (resource==null)
                    return;
                response.setContentType("text/css");
            }
            else
            {
                //no resource - try other handlers
                super.handle(target, baseRequest, request, response);
                return;
            }
        }
            
        // We are going to serve something
        baseRequest.setHandled(true);

        if (resource.isDirectory())
        {
            if (!request.getPathInfo().endsWith(URIUtil.SLASH))
            {
                response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths(request.getRequestURI(),URIUtil.SLASH)));
                return;
            }

            Resource welcome=getWelcome(resource);
            if (welcome!=null && welcome.exists())
                resource=welcome;
            else
            {
                doDirectory(request,response,resource);
                baseRequest.setHandled(true);
                return;
            }
        }

        // set some headers
        long last_modified=resource.lastModified();
        String etag=null;
        
        if (last_modified>0)
        {
            long if_modified=request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
            if (if_modified>0 && last_modified/1000<=if_modified/1000)
            {
                response.setStatus(HttpStatus.NOT_MODIFIED_304);
                return;
            }
        }

        // set the headers
        doResponseHeaders(response,resource,null);
        response.setDateHeader(HttpHeaders.LAST_MODIFIED,last_modified);
        
        if(skipContentBody)
            return;
        // Send the content
        OutputStream out =null;
        try {out = response.getOutputStream();}
        catch(IllegalStateException e) {out = new WriterOutputStream(response.getWriter());}

        // See if a short direct method can be used?
        if (out instanceof AbstractHttpConnection.Output)
        {
            // TODO file mapped buffers
            ((AbstractHttpConnection.Output)out).sendContent(resource.getInputStream());
        }
        else
        {
            // Write content normally
            resource.writeTo(out,0,resource.length());
        }
	}

}
