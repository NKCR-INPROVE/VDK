package cz.incad.vdkcommon;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 *
 * @author alberto
 */
public class SolrIndexerCommiter {

    private static HashMap <String, SolrServer> _servers = new HashMap<String, SolrServer>();

    public static SolrServer getServer() throws IOException {
        Options opts = Options.getInstance();
        return getServer(opts.getString("solrCore", "vdk_md5"));
    }

    public static SolrServer getServer(String core) throws IOException {
        if (!_servers.containsKey(core)) {
            Options opts = Options.getInstance();
             HttpSolrServer _server = new HttpSolrServer(String.format("%s/%s/",
                    opts.getString("solrHost", "http://localhost:8080/solr"),
                    core));
            _server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
            _server.setConnectionTimeout(5000); // 5 seconds to establish TCP

        // The following settings are provided here for completeness.
            // They will not normally be required, and should only be used 
            // after consulting javadocs to know whether they are truly required.
            _server.setSoTimeout(1000);  // socket read timeout
            _server.setDefaultMaxConnectionsPerHost(100);
            _server.setMaxTotalConnections(100);
            _server.setFollowRedirects(false);  // defaults to false

        // allowCompression defaults to false.
            // Server side must support gzip or deflate for this to have any effect.
            _server.setAllowCompression(true);
            _servers.put(core, _server);
        }
        return _servers.get(core);
    }

    public static String postData(String dataStr)
            throws Exception {
        Options opts = Options.getInstance();
        return postData(String.format("%s/%s/update",
                opts.getString("solrHost", "http://localhost:8080/solr"),
                opts.getString("solrCore", "vdk_md5")), dataStr);
    }

    public static String postData(String url, String dataStr)
            throws Exception {

        URL solrUrl = new URL(url);
        Reader data = new StringReader(dataStr);
        StringBuilder output = new StringBuilder();
        HttpURLConnection urlc = null;
        String POST_ENCODING = "UTF-8";

        urlc = (HttpURLConnection) solrUrl.openConnection();
        try {
            urlc.setRequestMethod("POST");
        } catch (ProtocolException e) {
            throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
        }
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setUseCaches(false);
        urlc.setAllowUserInteraction(false);
        urlc.setRequestProperty("Content-type", "text/xml; charset=" + POST_ENCODING);

        OutputStream out = urlc.getOutputStream();

        try {
            Writer writer = new OutputStreamWriter(out, POST_ENCODING);
            pipe(data, writer);
            writer.close();
        } catch (IOException e) {
            throw new Exception("IOException while posting data", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }

        InputStream in = urlc.getInputStream();
        int status = urlc.getResponseCode();
        StringBuilder errorStream = new StringBuilder();
        try {
            if (status != HttpURLConnection.HTTP_OK) {
                errorStream.append("postData URL=").append(solrUrl.toString()).append(" HTTP response code=").append(status).append(" ");
                throw new Exception("URL=" + solrUrl.toString() + " HTTP response code=" + status);
            }
            Reader reader = new InputStreamReader(in);
            pipeString(reader, output);
            reader.close();
        } catch (IOException e) {
            throw new Exception("IOException while reading response", e);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        InputStream es = urlc.getErrorStream();
        if (es != null) {
            try {
                Reader reader = new InputStreamReader(es);
                pipeString(reader, errorStream);
                reader.close();
            } catch (IOException e) {
                throw new Exception("IOException while reading response", e);
            } finally {
                if (es != null) {
                    es.close();
                }
            }
        }
        if (errorStream.length() > 0) {
            throw new Exception("postData error: " + errorStream.toString());
        }

        return output.toString();

    }

    /**
     * Pipes everything from the reader to the writer via a buffer
     */
    private static void pipe(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            writer.write(buf, 0, read);
        }
        writer.flush();
    }

    /**
     * Pipes everything from the reader to the writer via a buffer except lines
     * starting with '<?'
     */
    private static void pipeString(Reader reader, StringBuilder writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            if (!(buf[0] == '<' && buf[1] == '?')) {
                writer.append(buf, 0, read);
            }
        }
    }
}
