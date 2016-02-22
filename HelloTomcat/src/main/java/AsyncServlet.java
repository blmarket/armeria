import java.io.IOException;
import java.util.concurrent.Executors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true, urlPatterns = "/async")
public class AsyncServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final AsyncContext async = req.startAsync();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    async.getResponse().getWriter().append("OK").close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                async.complete();
            }
        });
    }
}