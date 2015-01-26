package generator

import groovy.transform.CompileStatic

import javax.print.DocPrintJob

/**
 * This class is responsible for downloading a documentation page as generated through the Asciidoctor task
 * of the Groovy build, then filter its contents in order to return only the body of the documentation, as HTML.
 *
 * @author Cédric Champeau
 */
@CompileStatic
class DocumentationHTMLCleaner {
    private final static String BODY_START = /<body/
    private final static String BODY_END = /<\/body/
    private final static String TOC_START = /<div id="toc"/
    private final static String MAIN_START = /<div id="content"/
    private final static String MAIN_END = /<div id="footer"/


    private static String cleanupPage(String location) {
        def url = location.toURL()
        try {
            def fullHTML = url.getText('utf-8')
            return extractBetween(fullHTML, BODY_START, BODY_END)
        } catch (FileNotFoundException e) {
            // 404 not found
        }

        null
    }

    private static String extractBetween(String html, String startString, String endString) {
        def start = html.indexOf(startString)
        if (start > 0) {
            start = html.indexOf('>', start) + 1
        }
        if (start > 1) {
            def end = html.indexOf(endString, start)
            if (end > start) {
                return html.substring(start, end)
            }
        }
        null
    }

    public static DocPage parsePage(String location) {
        String contents = cleanupPage(location)
        if (contents==null) {
            return new DocPage(content: "Contents not found for <a href='$location'>$location</a>")
        }
        String toc = extractTOC(contents)?:''
        String main = extractBetween(contents, MAIN_START, MAIN_END)?:"Main body not found for <a href='$location'>$location</a>"
        new DocPage(toc: toc, content: main)
    }

    private static String extractTOC(final String html) {
        int start = html.indexOf(TOC_START)
        if (start > 0) {
            int end = html.indexOf(MAIN_START)
            if (end>0) {
                return html.substring(start,end).replace("<div id=\"toctitle\">Table of Contents</div>","")
            }
        }
        null
    }

    static class DocPage {
        String toc = ''
        String content
    }

}
