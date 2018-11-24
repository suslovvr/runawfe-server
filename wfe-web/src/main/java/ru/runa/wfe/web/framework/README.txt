Classes here are the web framework, which:

- is responsible for dispatching requests to RequestHandler instances and for converting request parameters to java objects.

- contains no annotations, no XML configs, no magic, just simple stupid straight imperative java code.

Initially created by Dmitry Grigoriev (dimgel).


How to study:

1. Look through documentation below in this file; it will take 2 minutes.

2. Set a breakpoint on Servlet.service() and go through stepping into each call (except configuration.requestParameterParser.parse()
   which you better step over, at least for the first time) until RequestHandler is called; it will take another 2 minutes.


Core:

- class Servlet is entry point. Register it (or its fubclass if you like) in web.xml.
  Specify servlet init parameter "configurationClass" -- full class name of ServletConfiguration subclass.

- subclass of ServletConfiguration must have default constructor.
  It is responsible for constructing instances of UriToHandlerMapper and RequestParamsParser.

- class RequestHandler is abstract base class for all request handlers.
  See package ../extra for some useful subclasses.

- subclass of UriToHandlerMapping maps HttpServletRequest.getPathInfo() to RequestHandler instances, and collects path parameters along.
  I assume it's to be done by splitting URI to components (by '/' char) and doing nested switch() on each component;
  there is a helper class UriToHandlerMapping.PathComponents for that. Path parameters can only occupy whole component.

- subclass of RequestParamsParser parses path parameters and HttpServletRequest.getParametersMap() into new instance
  of RequestHandler.paramsClass. If you don't need application-specific parameter types or parameter parsing logic, you
  may instantiate RequestParamsParser itself in your ServletConfiguration subclass.


Extra (core does not depend on these):

- class Id is parameter class for REST URLs targeting single entity. It's a simplest example of parameter class.

- class JsonHandler (extends RequestHandler) is abstract base class for requests that respond with JSON.
  It uses Jackson to serialize arbitrary response objects to HttpServletResponse output stream.

- class JspHandler (extends RequestHandler) just forwards request to specified JSP.

- class RedirectHandler (extends RequestHandler) performs HTTP redirect. Accepts GET, no request parameters.
