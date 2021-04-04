package xyz.jienan.xkcd.model.util

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test


class XkcdExplainUtilsTest {

//    @Test
//    fun test2198ExplainExtraction() {
//        testExplainExtraction(resp2198, result2198)
//    }
//
//    @Test
//    fun test2207ExplainExtraction() {
//        testExplainExtraction(resp2207, result2207)
//    }

    private fun testExplainExtraction(resp: String, result: String) {
        val body = ResponseBody.create(
                MediaType.parse("text/html; charset=UTF-8"),
                resp
        )
        val a = result.trimIndent().replace(" ", "")
        val b = XkcdExplainUtil.getExplainFromHtml(body, "https://www.explainxkcd.com")!!.trimIndent().replace(" ", "")
        assertEquals(a, b)
    }

    private val result2198 = " <table style=\"background-color: white; border: 1px solid #aaa; box-shadow: 2px 2px 2px rgba(0, 0, 0, 0.2); border-left: 10px solid #1E90FF; margin: 0 auto;\" class=\"notice_tpl\"> \n" +
            " <tbody>\n" +
            "  <tr> \n" +
            "   <td> <img alt=\"Ambox notice.png\" src=\"/wiki/images/c/c8/Ambox_notice.png\" width=\"40\" height=\"40\"> </td> \n" +
            "   <td style=\"padding:0 1em\"> <b>This explanation may be incomplete or incorrect:</b> <i>Created by TWO UNKNOWNS. About half of the explanation seems insufficiently related to the comic. Do NOT delete this tag too soon.</i><br>If you can address this issue, please <b><a rel=\"nofollow\" class=\"external text\" href=\"xkcd://explain.edit\">edit the page</a>!</b> Thanks. </td>\n" +
            "   <br>\n" +
            "   <br>\n" +
            "  </tr>\n" +
            " </tbody>\n" +
            "</table><a href=\"https://www.explainxkcd.com/wiki/index.php/White_Hat\" title=\"White Hat\">White Hat</a> is observing a <a href=\"https://en.wikipedia.org/wiki/physicist\" class=\"extiw\" title=\"wikipedia:physicist\">physicist</a>, <a href=\"https://www.explainxkcd.com/wiki/index.php/Cueball\" title=\"Cueball\">Cueball</a>, who is staring at some (in the comic unreadable) equations and diagrams on a <a href=\"https://en.wikipedia.org/wiki/chalkboard\" class=\"extiw\" title=\"wikipedia:chalkboard\">chalkboard</a>. White Hat is neither a physicist nor a <a href=\"https://en.wikipedia.org/wiki/mathematician\" class=\"extiw\" title=\"wikipedia:mathematician\">mathematician</a>, and seems to glorify those professions. He wishes he understood Cueball's work and \"the beauty on display here.\" People who profess a love for mathematics often cite the beauty they see in pure math, how things work out so perfectly, as the reason they love math. <p>The joke is that Cueball as a physicist is doing something instead quite simple and relatable: Avoiding hard work. Solving many kinds of constraints for two unknowns isn't necessarily difficult, but can be depending on the details. Cueball clearly thinks a solution is possible but would rather find an easier route. The same could be said about the field of mathematics in general: A proof is beautiful to a mathematician when it provides <a href=\"https://en.wikipedia.org/wiki/aesthetic\" class=\"extiw\" title=\"wikipedia:aesthetic\">aesthetic</a> pleasure, usually associated with being easy to understand. A proof is elegant when it is both easy to understand and correct, and mathematical solutions are profound when useful. Record numbers of mathematics interest groups and their forums in which such work is done exist today, from academic journals predating the use of electricity to a plethora of internet math and science fora such as <a href=\"https://en.wikipedia.org/wiki/Wikipedia:Reference_desk/Mathematics\" class=\"extiw\" title=\"wikipedia:Wikipedia:Reference desk/Mathematics\">Wikipedia Reference Desks</a> and Reddit's <a rel=\"nofollow\" class=\"external text\" href=\"https://reddit.com/r/theydidthemath\">/r/theydidthemath</a> forum, which fueled a <a rel=\"nofollow\" class=\"external text\" href=\"https://i.imgur.com/l1r1VEE.png\">resurgence of the phrase \"they did the math\" as a search term in 2014,</a> because it was included in the sidebar of the <a rel=\"nofollow\" class=\"external text\" href=\"https://reddit.com/r/xkcd\">/r/xkcd</a> subreddit, where it remains five years hence, between \"Linguistics\" and \"Ask Historians\" suggesting that the term was popularized by Xkcd fans after <a rel=\"nofollow\" class=\"external text\" href=\"https://books.google.com/ngrams/graph?content=they+did+the+math&amp;case_insensitive=on&amp;year_start=1980&amp;year_end=2008&amp;corpus=15&amp;smoothing=3&amp;share=&amp;direct_url=t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0#t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0\">its initial appearance c. 1988.</a> The proliferation of mathematics fora is certainly also due to the quickly increasing overall level of education and rapidly growing numbers of internet users. </p><p>A mathematical problem involving two unknowns could be a <a href=\"https://en.wikipedia.org/wiki/system_of_linear_equations\" class=\"extiw\" title=\"wikipedia:system of linear equations\">system of linear equations</a> which can often be solved on paper, a blackboard, in a spreadsheet with solver functions, or by a <a href=\"https://en.wikipedia.org/wiki/computer_algebra_system\" class=\"extiw\" title=\"wikipedia:computer algebra system\">computer algebra system</a> such as <a rel=\"nofollow\" class=\"external text\" href=\"https://www.wolframalpha.com/input/?i=x%2By%3D10%2C+x-y%3D4&amp;lk=3\">WolframAlpha.com.</a> Linear equations are a typical kind of more general constraint satisfaction problems, which in turn are <a href=\"https://en.wikipedia.org/wiki/mathematical_optimization\" class=\"extiw\" title=\"wikipedia:mathematical optimization\">mathematical optimization</a> problems, where the minimization of a difference from a goal state (such as that all of the constraining equations are true, for example) indicates the extent to which constraints are met. Sometimes such problem solving activity arises naturally from economic transactions according to, for example, the laws of <a href=\"https://en.wikipedia.org/wiki/supply_and_demand\" class=\"extiw\" title=\"wikipedia:supply and demand\">supply and demand</a>, arising in the general context of civilization and ecology (both of which have properties associated with beauty and mathematical elegance.) Problems solved by economics are examples of <a href=\"https://en.wikipedia.org/wiki/distributed_constraint_optimization\" class=\"extiw\" title=\"wikipedia:distributed constraint optimization\">distributed constraint optimization</a> processes. When economic laws are not sufficiently satisfying constraints, that is a <a href=\"https://en.wikipedia.org/wiki/market_failure\" class=\"extiw\" title=\"wikipedia:market failure\">market failure</a>, which indicates that more artificial and manual mathematical work is required, instead of the naturally arising or otherwise automatic methods contemplated by Cueball. Other distributed constraint optimization systems can be <a href=\"https://en.wikipedia.org/wiki/crowdsourcing\" class=\"extiw\" title=\"wikipedia:crowdsourcing\">crowdsourcing</a> games, such as <a href=\"https://en.wikipedia.org/wiki/FoldIt\" class=\"extiw\" title=\"wikipedia:FoldIt\">FoldIt</a> and <a href=\"https://en.wikipedia.org/wiki/Galaxy_Zoo\" class=\"extiw\" title=\"wikipedia:Galaxy Zoo\">Galaxy Zoo</a>. </p><p>Of the graphic elements on the blackboard, the most distinctive appears to be a pair of wedges from a pie chart, where the radius of the slices is being used to represent another variable than the angles which all pie charts use to represent a primary variable. Since the cartoon is in black and white, the use of color to represent category labels or more variables may be ruled out. Such black-and-white wedges represent two variables, the meaning of which may be unknown to us, let alone their values. The only distributed constraint optimization game which uses such wedges may be the <a href=\"https://en.wikipedia.org/wiki/climate_stabilization_wedge\" class=\"extiw\" title=\"wikipedia:climate stabilization wedge\">climate stabilization wedge</a> game <a rel=\"nofollow\" class=\"external text\" href=\"https://cmi.princeton.edu/wedges/game\">from Princeton University.</a> In that wedge game, angles represent a potential number of gigatons of atmospheric carbon mitigation (out of about 38 for the circle) and radius indicates uptake, or the extent to which the mitigation solution is effective. </p><p>That game is an example of a bivariate optimization problem which might not have to be manually solved by anyone, for example under specific assumptions about the market in <a rel=\"nofollow\" class=\"external text\" href=\"https://x.company/projects/foghorn\">Project Foghorn</a> <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/WlkWabq/ioc-part-1-prototype-article-in-press.pdf\">plants</a> and <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/SRxC3bd/power-to-gas-efficiency.pdf\">power-to-gas upgrades for natural gas</a> power plants. If such market-based approaches to distributed constraint satisfaction are successful, then the work in finding the solution would be performed not entirely by physicists, chemical engineers, mathematicians, or intentional crowdworkers playing a game to achieve the optimal solution(s), but instead in even larger part by far more widely distributed crowdworkers who are simply making their own, ideally self-interested choices regarding their demand for <a href=\"https://en.wikipedia.org/wiki/desalination\" class=\"extiw\" title=\"wikipedia:desalination\">desalinated</a> and <a href=\"https://en.wikipedia.org/wiki/drinking_water\" class=\"extiw\" title=\"wikipedia:drinking water\">potable water</a>, <a href=\"https://en.wikipedia.org/wiki/carbon-neutral_fuel\" class=\"extiw\" title=\"wikipedia:carbon-neutral fuel\">carbon-neutral liquid transportation fuel</a> and carbon-negative <a href=\"https://en.wikipedia.org/wiki/carbon_sequestration\" class=\"extiw\" title=\"wikipedia:carbon sequestration\">sequestration</a> in <a href=\"https://en.wikipedia.org/wiki/fiber-reinforced_composite\" class=\"extiw\" title=\"wikipedia:fiber-reinforced composite\">fiber-reinforced composite</a> lumber, both made from <a href=\"https://en.wikipedia.org/wiki/ocean_acidification\" class=\"extiw\" title=\"wikipedia:ocean acidification\">carbonate dissolved in seawater</a>, and for recycling the carbon in power plant flue exhaust for the <a href=\"https://en.wikipedia.org/wiki/Energy_storage\" class=\"extiw\" title=\"wikipedia:Energy storage\">storage of renewable energy</a> such as off-peak <a href=\"https://en.wikipedia.org/wiki/wind_power\" class=\"extiw\" title=\"wikipedia:wind power\">wind power</a>. The relative beauty, elegance, and simplicity of the possible solutions to such problems are subjective, and might involve strong differences of opinion between outside observers, mathematicians and engineers involved with the details, and <a href=\"https://en.wikipedia.org/wiki/Villain#Sympathetic_villain\" class=\"extiw\" title=\"wikipedia:Villain\">fossil fuel barons</a>, respected and enriched by society for their part in meeting energy demand. (See \"All Chemistry Equations\" in <a href=\"https://www.explainxkcd.com/wiki/index.php/2034:_Equations\" title=\"2034: Equations\">2034: Equations</a>.) Although the original market-focused primary use of <a href=\"https://en.wikipedia.org/wiki/ticker_tape\" class=\"extiw\" title=\"wikipedia:ticker tape\">ticker tape</a> may be a lost art, the economy is still driven by individual free will leveraging self-interested behavior to achieve social gains for civilization. </p><p>The title text continues Cueball's thought process, with the possibility of using an automatic equation solver to find the unknowns. Equation solvers are not often considered beautiful ways to address purely mathematical problems, even if they are often the most efficient and in that sense elegant solutions to applied problems in engineering. Using a formal solver with symbolic, numeric, or both methods requires making sure that the constraints (e.g. equations) are entered correctly, with parentheses balanced in their correct locations for the solution to succeed. While the <a href=\"https://en.wikipedia.org/wiki/mathematical_beauty\" class=\"extiw\" title=\"wikipedia:mathematical beauty\">beauty of mathematics</a> and pure physics may not be associated with automatic solvers in spreadsheets, general optimization methods are considered elegant in applied physics and engineering, with <a rel=\"nofollow\" class=\"external text\" href=\"http://entsphere.com/pub/pdf/1957%20Jaynes,%20ShannonMaxEntBoltzmann.pdf\">Jaynes (1957)</a> cited more than 12,000 times on Google Scholar, including by <a rel=\"nofollow\" class=\"external text\" href=\"https://www.researchgate.net/publication/234147180_Maximum_Entropy_Image_Restoration_in_Astronomy\">a paper cited</a> by the <a rel=\"nofollow\" class=\"external text\" href=\"https://arxiv.org/abs/1711.01286\">first black hole image astronomers</a> for example. </p> "

    private val resp2198 = "<!doctype html>\n" +
            "<html class=\"client-nojs\" lang=\"en\" dir=\"ltr\">\n" +
            " <head> \n" +
            "  <meta charset=\"UTF-8\"> \n" +
            "  <title>2207: Math Work - explain xkcd</title> \n" +
            "  <script src=\"/cdn-cgi/apps/head/M52ISAAYfDYfNhlAeg3pMasjGfw.js\"></script>\n" +
            "  <script>document.documentElement.className = document.documentElement.className.replace( /(^|\\s)client-nojs(\\s|\$)/, \"\$1client-js\$2\" );</script> \n" +
            "  <script>(window.RLQ=window.RLQ||[]).push(function(){mw.config.set({\"wgCanonicalNamespace\":\"\",\"wgCanonicalSpecialPageName\":false,\"wgNamespaceNumber\":0,\"wgPageName\":\"2207:_Math_Work\",\"wgTitle\":\"2207: Math Work\",\"wgCurRevisionId\":180723,\"wgRevisionId\":180723,\"wgArticleId\":22383,\"wgIsArticle\":true,\"wgIsRedirect\":false,\"wgAction\":\"view\",\"wgUserName\":null,\"wgUserGroups\":[\"*\"],\"wgCategories\":[\"All comics\",\"Comics from 2019\",\"Comics from September\",\"Wednesday comics\",\"Incomplete explanations\",\"Comics featuring White Hat\",\"Comics featuring Cueball\",\"Physics\",\"Math\"],\"wgBreakFrames\":false,\"wgPageContentLanguage\":\"en\",\"wgPageContentModel\":\"wikitext\",\"wgSeparatorTransformTable\":[\"\",\"\"],\"wgDigitTransformTable\":[\"\",\"\"],\"wgDefaultDateFormat\":\"dmy\",\"wgMonthNames\":[\"\",\"January\",\"February\",\"March\",\"April\",\"May\",\"June\",\"July\",\"August\",\"September\",\"October\",\"November\",\"December\"],\"wgMonthNamesShort\":[\"\",\"Jan\",\"Feb\",\"Mar\",\"Apr\",\"May\",\"Jun\",\"Jul\",\"Aug\",\"Sep\",\"Oct\",\"Nov\",\"Dec\"],\"wgRelevantPageName\":\"2207:_Math_Work\",\"wgRelevantArticleId\":22383,\"wgRequestId\":\"a21ab34392336f53728e29fe\",\"wgIsProbablyEditable\":true,\"wgRelevantPageIsProbablyEditable\":true,\"wgRestrictionEdit\":[],\"wgRestrictionMove\":[],\"wgRedirectedFrom\":\"2207\",\"wgInternalRedirectTargetUrl\":\"/wiki/index.php/2207:_Math_Work\"});mw.loader.state({\"site.styles\":\"ready\",\"noscript\":\"ready\",\"user.styles\":\"ready\",\"user\":\"ready\",\"user.options\":\"loading\",\"user.tokens\":\"loading\",\"mediawiki.legacy.shared\":\"ready\",\"mediawiki.legacy.commonPrint\":\"ready\",\"mediawiki.sectionAnchor\":\"ready\",\"mediawiki.skinning.interface\":\"ready\",\"skins.vector.styles\":\"ready\"});mw.loader.implement(\"user.options@0bhc5ha\",function(\$,jQuery,require,module){mw.user.options.set([]);});mw.loader.implement(\"user.tokens@0yp1lfe\",function ( \$, jQuery, require, module ) {\n" +
            "mw.user.tokens.set({\"editToken\":\"+\\\\\",\"patrolToken\":\"+\\\\\",\"watchToken\":\"+\\\\\",\"csrfToken\":\"+\\\\\"});/*@nomin*/\n" +
            "\n" +
            "});mw.loader.load([\"mediawiki.action.view.redirect\",\"site\",\"mediawiki.page.startup\",\"mediawiki.user\",\"mediawiki.hidpi\",\"mediawiki.page.ready\",\"mediawiki.searchSuggest\",\"skins.vector.js\"]);});</script> \n" +
            "  <link rel=\"stylesheet\" href=\"/wiki/load.php?debug=false&amp;lang=en&amp;modules=mediawiki.legacy.commonPrint%2Cshared%7Cmediawiki.sectionAnchor%7Cmediawiki.skinning.interface%7Cskins.vector.styles&amp;only=styles&amp;skin=vector\"> \n" +
            "  <script async src=\"/wiki/load.php?debug=false&amp;lang=en&amp;modules=startup&amp;only=scripts&amp;skin=vector\"></script> \n" +
            "  <meta name=\"ResourceLoaderDynamicStyles\" content=\"\"> \n" +
            "  <link rel=\"stylesheet\" href=\"/wiki/load.php?debug=false&amp;lang=en&amp;modules=site.styles&amp;only=styles&amp;skin=vector\"> \n" +
            "  <meta name=\"generator\" content=\"MediaWiki 1.30.0\"> \n" +
            "  <meta name=\"description\" content=\"Explain xkcd is a wiki dedicated to explaining the webcomic xkcd. Go figure.\"> \n" +
            "  <link rel=\"alternate\" type=\"application/x-wiki\" title=\"Edit\" href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit\"> \n" +
            "  <link rel=\"edit\" title=\"Edit\" href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit\"> \n" +
            "  <link rel=\"shortcut icon\" href=\"/wiki/images/0/04/16px-BlackHat_head.png\"> \n" +
            "  <link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"/wiki/opensearch_desc.php\" title=\"explain xkcd (en)\"> \n" +
            "  <link rel=\"EditURI\" type=\"application/rsd+xml\" href=\"//www.explainxkcd.com/wiki/api.php?action=rsd\"> \n" +
            "  <link rel=\"alternate\" type=\"application/atom+xml\" title=\"explain xkcd Atom feed\" href=\"/wiki/index.php?title=Special:RecentChanges&amp;feed=atom\"> \n" +
            "  <link rel=\"canonical\" href=\"https://www.explainxkcd.com/wiki/index.php/2207:_Math_Work\"> \n" +
            "  <!--[if lt IE 9]><script src=\"/resources/lib/html5shiv/html5shiv.min.js\"></script><![endif]--> \n" +
            " </head> \n" +
            " <body class=\"mediawiki ltr sitedir-ltr mw-hide-empty-elt ns-0 ns-subject page-2207_Math_Work rootpage-2207_Math_Work skin-vector action-view\"> \n" +
            "  <div id=\"mw-page-base\" class=\"noprint\"></div> \n" +
            "  <div id=\"mw-head-base\" class=\"noprint\"></div> \n" +
            "  <div id=\"content\" class=\"mw-body\" role=\"main\"> \n" +
            "   <a id=\"top\"></a> \n" +
            "   <div id=\"siteNotice\" class=\"mw-body-content\">\n" +
            "    <div id=\"mw-dismissablenotice-anonplace\"></div>\n" +
            "    <script>(function(){var node=document.getElementById(\"mw-dismissablenotice-anonplace\");if(node){node.outerHTML=\"\\u003Cdiv id=\\\"localNotice\\\" lang=\\\"en\\\" dir=\\\"ltr\\\"\\u003E\\u003Cdiv class=\\\"mw-parser-output\\\"\\u003E\\u003Cdiv class=\\\"plainlinks\\\" style=\\\"background:#f5faff; border:1px solid #a7d7f9; margin:1em auto 1em auto; width:100%; font-size: 120%; padding: 0.5ex; text-align: center;\\\"\\u003E\\n\\u003Cp\\u003EWe still need to complete some explanations like this one: \\u003Ca href=\\\"/wiki/index.php/1688:_Map_Age_Guide\\\" title=\\\"1688: Map Age Guide\\\"\\u003E1688: Map Age Guide\\u003C/a\\u003E. All incomplete explanations are \\u003Ca href=\\\"/wiki/index.php/Category:Incomplete_explanations\\\" title=\\\"Category:Incomplete explanations\\\"\\u003Ehere\\u003C/a\\u003E.\\n\\u003C/p\\u003E\\u003Cp\\u003ENever use the \\u003Ci\\u003EMath markup language\\u003C/i\\u003E at the transcript. The reason for this you can read at the \\u003Ca href=\\\"/wiki/index.php/explain_xkcd:Editor_FAQ\\\" title=\\\"explain xkcd:Editor FAQ\\\"\\u003EEditor FAQ\\u003C/a\\u003E.\\n\\u003C/p\\u003E\\n\\u003C/div\\u003E\\n\\u003C/div\\u003E\\u003C/div\\u003E\";}}());</script>\n" +
            "   </div> \n" +
            "   <div class=\"mw-indicators mw-body-content\"> \n" +
            "   </div> \n" +
            "   <h1 id=\"firstHeading\" class=\"firstHeading\" lang=\"en\">2207: Math Work</h1> \n" +
            "   <div id=\"bodyContent\" class=\"mw-body-content\"> \n" +
            "    <div id=\"siteSub\" class=\"noprint\">\n" +
            "     Explain xkcd: It's 'cause you're dumb.\n" +
            "    </div> \n" +
            "    <div id=\"contentSub\">\n" +
            "     <span class=\"mw-redirectedfrom\">(Redirected from <a href=\"/wiki/index.php?title=2207&amp;redirect=no\" class=\"mw-redirect\" title=\"2207\">2207</a>)</span>\n" +
            "    </div> \n" +
            "    <div id=\"jump-to-nav\" class=\"mw-jump\">\n" +
            "      Jump to: \n" +
            "     <a href=\"#mw-head\">navigation</a>, \n" +
            "     <a href=\"#p-search\">search</a> \n" +
            "    </div> \n" +
            "    <div id=\"mw-content-text\" lang=\"en\" dir=\"ltr\" class=\"mw-content-ltr\">\n" +
            "     <div class=\"mw-parser-output\">\n" +
            "      <table class=\"\" cellspacing=\"5\" style=\"background-color: #FFFFFF; border: 1px solid #AAAAAA; color: black; font-size: 88%; line-height: 1.5em; margin: 0.5em 0 0.5em 1em; padding: 0.2em; text-align: center; width:98%;\">\n" +
            "       <tbody>\n" +
            "        <tr>\n" +
            "         <td>\n" +
            "          <ul style=\"text-align: center; margin-bottom: 10px;\" class=\"no-link-underline\">\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\"><a href=\"/wiki/index.php/1\" class=\"mw-redirect\" title=\"1\"><span style=\"color: #FFFFFF; padding: 0 12px;\">|&lt;</span></a></li>\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\"><a href=\"/wiki/index.php/2206\" class=\"mw-redirect\" title=\"2206\"><span style=\"color: #FFFFFF; padding: 0 12px;\">&lt;&nbsp;Prev</span></a></li>\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\" class=\"plainlinks\"><a rel=\"nofollow\" class=\"external text\" href=\"https://www.xkcd.com/2207/\"><span style=\"color: #FFFFFF; padding: 0 12px;\">Comic&nbsp;#2207&nbsp;(September&nbsp;25,&nbsp;2019)</span></a></li>\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\"><a href=\"/wiki/index.php/2208\" class=\"mw-redirect\" title=\"2208\"><span style=\"color: #FFFFFF; padding: 0 12px;\">Next&nbsp;&gt;</span></a></li>\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\"><a href=\"/wiki/index.php/2210\" class=\"mw-redirect\" title=\"2210\"><span style=\"color: #FFFFFF; padding: 0 12px;\">&gt;|</span></a></li>\n" +
            "          </ul></td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "         <td style=\"font-size: 20px; padding-bottom:10px\"><b>Math Work</b></td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "         <td><a href=\"/wiki/index.php/File:math_work.png\" class=\"image\" title=\"I could type this into a solver, which MIGHT help, but would also mean I have to get a lot of parentheses right...\"><img alt=\"I could type this into a solver, which MIGHT help, but would also mean I have to get a lot of parentheses right...\" src=\"/wiki/images/1/14/math_work.png\" width=\"612\" height=\"401\"></a><br><span style=\"\"><span style=\"color:grey\">Title text:</span> I could type this into a solver, which MIGHT help, but would also mean I have to get a lot of parentheses right...</span></td>\n" +
            "        </tr>\n" +
            "       </tbody>\n" +
            "      </table> \n" +
            "      <h2><span class=\"mw-headline\" id=\"Explanation\">Explanation</span><span class=\"mw-editsection\"><span class=\"mw-editsection-bracket\">[</span><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit&amp;section=1\" title=\"Edit section: Explanation\">edit</a><span class=\"mw-editsection-bracket\">]</span></span></h2> \n" +
            "      <table style=\"background-color: white; border: 1px solid #aaa; box-shadow: 2px 2px 2px rgba(0, 0, 0, 0.2); border-left: 10px solid #1E90FF; margin: 0 auto;\" class=\"notice_tpl\"> \n" +
            "       <tbody>\n" +
            "        <tr> \n" +
            "         <td> <img alt=\"Ambox notice.png\" src=\"/wiki/images/c/c8/Ambox_notice.png\" width=\"40\" height=\"40\"> </td> \n" +
            "         <td style=\"padding:0 1em\"> <b>This explanation may be incomplete or incorrect:</b> <i>Created by TWO UNKNOWNS. About half of the explanation seems insufficiently related to the comic. Do NOT delete this tag too soon.</i><br>If you can address this issue, please <b><a rel=\"nofollow\" class=\"external text\" href=\"//www.explainxkcd.com/wiki/index.php?title=2207:_Math_Work&amp;action=edit\">edit the page</a>!</b> Thanks. </td>\n" +
            "        </tr>\n" +
            "       </tbody>\n" +
            "      </table>\n" +
            "      <a href=\"/wiki/index.php/White_Hat\" title=\"White Hat\">White Hat</a> is observing a \n" +
            "      <a href=\"https://en.wikipedia.org/wiki/physicist\" class=\"extiw\" title=\"wikipedia:physicist\">physicist</a>, \n" +
            "      <a href=\"/wiki/index.php/Cueball\" title=\"Cueball\">Cueball</a>, who is staring at some (in the comic unreadable) equations and diagrams on a \n" +
            "      <a href=\"https://en.wikipedia.org/wiki/chalkboard\" class=\"extiw\" title=\"wikipedia:chalkboard\">chalkboard</a>. White Hat is neither a physicist nor a \n" +
            "      <a href=\"https://en.wikipedia.org/wiki/mathematician\" class=\"extiw\" title=\"wikipedia:mathematician\">mathematician</a>, and seems to glorify those professions. He wishes he understood Cueball's work and \"the beauty on display here.\" People who profess a love for mathematics often cite the beauty they see in pure math, how things work out so perfectly, as the reason they love math. \n" +
            "      <p>The joke is that Cueball as a physicist is doing something instead quite simple and relatable: Avoiding hard work. Solving many kinds of constraints for two unknowns isn't necessarily difficult, but can be depending on the details. Cueball clearly thinks a solution is possible but would rather find an easier route. The same could be said about the field of mathematics in general: A proof is beautiful to a mathematician when it provides <a href=\"https://en.wikipedia.org/wiki/aesthetic\" class=\"extiw\" title=\"wikipedia:aesthetic\">aesthetic</a> pleasure, usually associated with being easy to understand. A proof is elegant when it is both easy to understand and correct, and mathematical solutions are profound when useful. Record numbers of mathematics interest groups and their forums in which such work is done exist today, from academic journals predating the use of electricity to a plethora of internet math and science fora such as <a href=\"https://en.wikipedia.org/wiki/Wikipedia:Reference_desk/Mathematics\" class=\"extiw\" title=\"wikipedia:Wikipedia:Reference desk/Mathematics\">Wikipedia Reference Desks</a> and Reddit's <a rel=\"nofollow\" class=\"external text\" href=\"https://reddit.com/r/theydidthemath\">/r/theydidthemath</a> forum, which fueled a <a rel=\"nofollow\" class=\"external text\" href=\"https://i.imgur.com/l1r1VEE.png\">resurgence of the phrase \"they did the math\" as a search term in 2014,</a> because it was included in the sidebar of the <a rel=\"nofollow\" class=\"external text\" href=\"https://reddit.com/r/xkcd\">/r/xkcd</a> subreddit, where it remains five years hence, between \"Linguistics\" and \"Ask Historians\" suggesting that the term was popularized by Xkcd fans after <a rel=\"nofollow\" class=\"external text\" href=\"https://books.google.com/ngrams/graph?content=they+did+the+math&amp;case_insensitive=on&amp;year_start=1980&amp;year_end=2008&amp;corpus=15&amp;smoothing=3&amp;share=&amp;direct_url=t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0#t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0\">its initial appearance c. 1988.</a> The proliferation of mathematics fora is certainly also due to the quickly increasing overall level of education and rapidly growing numbers of internet users. </p>\n" +
            "      <p>A mathematical problem involving two unknowns could be a <a href=\"https://en.wikipedia.org/wiki/system_of_linear_equations\" class=\"extiw\" title=\"wikipedia:system of linear equations\">system of linear equations</a> which can often be solved on paper, a blackboard, in a spreadsheet with solver functions, or by a <a href=\"https://en.wikipedia.org/wiki/computer_algebra_system\" class=\"extiw\" title=\"wikipedia:computer algebra system\">computer algebra system</a> such as <a rel=\"nofollow\" class=\"external text\" href=\"https://www.wolframalpha.com/input/?i=x%2By%3D10%2C+x-y%3D4&amp;lk=3\">WolframAlpha.com.</a> Linear equations are a typical kind of more general constraint satisfaction problems, which in turn are <a href=\"https://en.wikipedia.org/wiki/mathematical_optimization\" class=\"extiw\" title=\"wikipedia:mathematical optimization\">mathematical optimization</a> problems, where the minimization of a difference from a goal state (such as that all of the constraining equations are true, for example) indicates the extent to which constraints are met. Sometimes such problem solving activity arises naturally from economic transactions according to, for example, the laws of <a href=\"https://en.wikipedia.org/wiki/supply_and_demand\" class=\"extiw\" title=\"wikipedia:supply and demand\">supply and demand</a>, arising in the general context of civilization and ecology (both of which have properties associated with beauty and mathematical elegance.) Problems solved by economics are examples of <a href=\"https://en.wikipedia.org/wiki/distributed_constraint_optimization\" class=\"extiw\" title=\"wikipedia:distributed constraint optimization\">distributed constraint optimization</a> processes. When economic laws are not sufficiently satisfying constraints, that is a <a href=\"https://en.wikipedia.org/wiki/market_failure\" class=\"extiw\" title=\"wikipedia:market failure\">market failure</a>, which indicates that more artificial and manual mathematical work is required, instead of the naturally arising or otherwise automatic methods contemplated by Cueball. Other distributed constraint optimization systems can be <a href=\"https://en.wikipedia.org/wiki/crowdsourcing\" class=\"extiw\" title=\"wikipedia:crowdsourcing\">crowdsourcing</a> games, such as <a href=\"https://en.wikipedia.org/wiki/FoldIt\" class=\"extiw\" title=\"wikipedia:FoldIt\">FoldIt</a> and <a href=\"https://en.wikipedia.org/wiki/Galaxy_Zoo\" class=\"extiw\" title=\"wikipedia:Galaxy Zoo\">Galaxy Zoo</a>. </p>\n" +
            "      <p>Of the graphic elements on the blackboard, the most distinctive appears to be a pair of wedges from a pie chart, where the radius of the slices is being used to represent another variable than the angles which all pie charts use to represent a primary variable. Since the cartoon is in black and white, the use of color to represent category labels or more variables may be ruled out. Such black-and-white wedges represent two variables, the meaning of which may be unknown to us, let alone their values. The only distributed constraint optimization game which uses such wedges may be the <a href=\"https://en.wikipedia.org/wiki/climate_stabilization_wedge\" class=\"extiw\" title=\"wikipedia:climate stabilization wedge\">climate stabilization wedge</a> game <a rel=\"nofollow\" class=\"external text\" href=\"https://cmi.princeton.edu/wedges/game\">from Princeton University.</a> In that wedge game, angles represent a potential number of gigatons of atmospheric carbon mitigation (out of about 38 for the circle) and radius indicates uptake, or the extent to which the mitigation solution is effective. </p>\n" +
            "      <p>That game is an example of a bivariate optimization problem which might not have to be manually solved by anyone, for example under specific assumptions about the market in <a rel=\"nofollow\" class=\"external text\" href=\"https://x.company/projects/foghorn\">Project Foghorn</a> <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/WlkWabq/ioc-part-1-prototype-article-in-press.pdf\">plants</a> and <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/SRxC3bd/power-to-gas-efficiency.pdf\">power-to-gas upgrades for natural gas</a> power plants. If such market-based approaches to distributed constraint satisfaction are successful, then the work in finding the solution would be performed not entirely by physicists, chemical engineers, mathematicians, or intentional crowdworkers playing a game to achieve the optimal solution(s), but instead in even larger part by far more widely distributed crowdworkers who are simply making their own, ideally self-interested choices regarding their demand for <a href=\"https://en.wikipedia.org/wiki/desalination\" class=\"extiw\" title=\"wikipedia:desalination\">desalinated</a> and <a href=\"https://en.wikipedia.org/wiki/drinking_water\" class=\"extiw\" title=\"wikipedia:drinking water\">potable water</a>, <a href=\"https://en.wikipedia.org/wiki/carbon-neutral_fuel\" class=\"extiw\" title=\"wikipedia:carbon-neutral fuel\">carbon-neutral liquid transportation fuel</a> and carbon-negative <a href=\"https://en.wikipedia.org/wiki/carbon_sequestration\" class=\"extiw\" title=\"wikipedia:carbon sequestration\">sequestration</a> in <a href=\"https://en.wikipedia.org/wiki/fiber-reinforced_composite\" class=\"extiw\" title=\"wikipedia:fiber-reinforced composite\">fiber-reinforced composite</a> lumber, both made from <a href=\"https://en.wikipedia.org/wiki/ocean_acidification\" class=\"extiw\" title=\"wikipedia:ocean acidification\">carbonate dissolved in seawater</a>, and for recycling the carbon in power plant flue exhaust for the <a href=\"https://en.wikipedia.org/wiki/Energy_storage\" class=\"extiw\" title=\"wikipedia:Energy storage\">storage of renewable energy</a> such as off-peak <a href=\"https://en.wikipedia.org/wiki/wind_power\" class=\"extiw\" title=\"wikipedia:wind power\">wind power</a>. The relative beauty, elegance, and simplicity of the possible solutions to such problems are subjective, and might involve strong differences of opinion between outside observers, mathematicians and engineers involved with the details, and <a href=\"https://en.wikipedia.org/wiki/Villain#Sympathetic_villain\" class=\"extiw\" title=\"wikipedia:Villain\">fossil fuel barons</a>, respected and enriched by society for their part in meeting energy demand. (See \"All Chemistry Equations\" in <a href=\"/wiki/index.php/2034:_Equations\" title=\"2034: Equations\">2034: Equations</a>.) Although the original market-focused primary use of <a href=\"https://en.wikipedia.org/wiki/ticker_tape\" class=\"extiw\" title=\"wikipedia:ticker tape\">ticker tape</a> may be a lost art, the economy is still driven by individual free will leveraging self-interested behavior to achieve social gains for civilization. </p>\n" +
            "      <p>The title text continues Cueball's thought process, with the possibility of using an automatic equation solver to find the unknowns. Equation solvers are not often considered beautiful ways to address purely mathematical problems, even if they are often the most efficient and in that sense elegant solutions to applied problems in engineering. Using a formal solver with symbolic, numeric, or both methods requires making sure that the constraints (e.g. equations) are entered correctly, with parentheses balanced in their correct locations for the solution to succeed. While the <a href=\"https://en.wikipedia.org/wiki/mathematical_beauty\" class=\"extiw\" title=\"wikipedia:mathematical beauty\">beauty of mathematics</a> and pure physics may not be associated with automatic solvers in spreadsheets, general optimization methods are considered elegant in applied physics and engineering, with <a rel=\"nofollow\" class=\"external text\" href=\"http://entsphere.com/pub/pdf/1957%20Jaynes,%20ShannonMaxEntBoltzmann.pdf\">Jaynes (1957)</a> cited more than 12,000 times on Google Scholar, including by <a rel=\"nofollow\" class=\"external text\" href=\"https://www.researchgate.net/publication/234147180_Maximum_Entropy_Image_Restoration_in_Astronomy\">a paper cited</a> by the <a rel=\"nofollow\" class=\"external text\" href=\"https://arxiv.org/abs/1711.01286\">first black hole image astronomers</a> for example. </p> \n" +
            "      <h2><span class=\"mw-headline\" id=\"Transcript\">Transcript</span><span class=\"mw-editsection\"><span class=\"mw-editsection-bracket\">[</span><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit&amp;section=2\" title=\"Edit section: Transcript\">edit</a><span class=\"mw-editsection-bracket\">]</span></span></h2> \n" +
            "      <dl>\n" +
            "       <dd>\n" +
            "        [White Hat is watching Cueball from a couple of meters away. Cueball is contemplating the formulas and diagrams that fills the blackboard he stands in front of. Cueball holds a chalk in his hand. None of the content on the blackboard is readable, but there is a diagram in the shape of a circle and a another pie shaped diagram. Both are thinking with large thought bubbles above their heads, with small bubbles connecting them and the larger bubble]\n" +
            "       </dd> \n" +
            "       <dd>\n" +
            "        White Hat (thinking): Amazing watching a physicist at work, exploring universes in a symphony of numbers.\n" +
            "       </dd> \n" +
            "       <dd>\n" +
            "        White Hat (thinking): If only I had studied math, I could appreciate the beauty on display here.\n" +
            "       </dd>\n" +
            "      </dl> \n" +
            "      <dl>\n" +
            "       <dd>\n" +
            "        Cueball (thinking): Oh no. This has \n" +
            "        <i><b>two</b></i> unknowns. That's gonna be really hard.\n" +
            "       </dd> \n" +
            "       <dd>\n" +
            "        Cueball (thinking): Ughhhhhhh.\n" +
            "       </dd> \n" +
            "       <dd>\n" +
            "        Cueball (thinking): \n" +
            "        <i><b>Think.</b></i> There's gotta be a way to avoid doing all that work...\n" +
            "       </dd>\n" +
            "      </dl> \n" +
            "      <p><br> </p> \n" +
            "      <span id=\"Discussion\"></span>\n" +
            "      <span style=\"position:absolute; right:0; padding-top:1em;\"><img alt=\"comment.png\" src=\"/wiki/images/0/03/comment.png\" width=\"16\" height=\"16\">&nbsp;<a rel=\"nofollow\" class=\"external text\" href=\"//www.explainxkcd.com/wiki/index.php?title=Talk:2207:_Math_Work&amp;action=edit\"><b>add a comment!</b></a>&nbsp;⋅&nbsp;<img alt=\"comment.png\" src=\"/wiki/images/0/03/comment.png\" width=\"16\" height=\"16\">&nbsp;<a rel=\"nofollow\" class=\"external text\" href=\"//www.explainxkcd.com/wiki/index.php?title=Talk:2207:_Math_Work&amp;action=edit&amp;section=new\"><b>add a topic (use sparingly)!</b></a>&nbsp;⋅&nbsp;<img alt=\"Icons-mini-action refresh blue.gif\" src=\"/wiki/images/e/e5/Icons-mini-action_refresh_blue.gif\" width=\"16\" height=\"16\">&nbsp;<a rel=\"nofollow\" class=\"external text\" href=\"//www.explainxkcd.com/wiki/index.php?title=2207:_Math_Work&amp;action=purge\"><b>refresh comments!</b></a></span>\n" +
            "      <h1><span class=\"mw-headline\" id=\"Discussion\">Discussion</span></h1>\n" +
            "      <div style=\"border:1px solid grey; background:#eee; padding:1em;\"> \n" +
            "       <p>This makes me think of my profession (software engineer) - Normie: \"Oh wow, that looks complicated!\" Me: wires two pre-existing libraries together and calls it a day <a href=\"/wiki/index.php?title=User:Baldrickk&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User:Baldrickk (page does not exist)\">Baldrickk</a> (<a href=\"/wiki/index.php?title=User_talk:Baldrickk&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User talk:Baldrickk (page does not exist)\">talk</a>) 09:39, 26 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dt>\n" +
            "         Image of Blackboard\n" +
            "        </dt>\n" +
            "       </dl> \n" +
            "       <p>I was looking at the blackboard and was wondering if there were any Easter eggs on it. Here is the result of my badly cropped photoshopping skills. <a rel=\"nofollow\" class=\"external autonumber\" href=\"https://drive.google.com/open?id=1kGCrQehNGksE2cSK1WvTJcgdwaZ5cdWe\">[1]</a> idk if it would help to sharpen the image. --<a href=\"/wiki/index.php?title=User:DarkAndromeda31&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User:DarkAndromeda31 (page does not exist)\">DarkAndromeda31</a> (<a href=\"/wiki/index.php?title=User_talk:DarkAndromeda31&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User talk:DarkAndromeda31 (page does not exist)\">talk</a>) 01:25, 26 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dd>\n" +
            "         The only thing that really jumps out at me are the wedges, as portions of pie charts where radius also controls area, evoking the \n" +
            "         <a href=\"https://en.wikipedia.org/wiki/climate_stabilization_wedge\" class=\"extiw\" title=\"wikipedia:climate stabilization wedge\">climate stabilization wedge</a> game \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://cmi.princeton.edu/wedges/game\">from Princeton</a> where the total area of the disk needing to be mitigated is something like 38 gigatons of atmospheric carbon, and the various mitigation solutions have angles representing potential and radius indicating uptake, the proportion of which represents gigatons mitigated as the wedge area. We can offer that game as an example of a bivariate optimization problem which might not have to be manually solved by anyone, if we assume that the local market for \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://drive.google.com/file/d/1ritJrcDKyXNe4Kp2dHBWiFuyBEHvn_81/view\">surplus potable water, carbon-neutral liquid transportation fuel, and carbon-negative composite lumber for centuries-to-millenia scale sequestration along with wood timber displacement for reforestation</a> represents locally satisfiable economic demand for N shipping containers of \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://x.company/projects/foghorn\">Project Foghorn</a> \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/WlkWabq/ioc-part-1-prototype-article-in-press.pdf\">plants</a> and M shipping containers of \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/SRxC3bd/power-to-gas-efficiency.pdf\">power-to-gas upgrades for natural gas</a> power plants. That's an example of how a locally market-driven system can solve a bivariate optimization without anyone doing the actual math work in a spreadsheet or otherwise. The economic solution is not necessarily optimal, because even \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://twitter.com/jsalsman/status/1118030378747351040\">as powerful as the free market can be,</a> it isn't necessarily going to find the bivariate optimums for every point on the planet (although it will likely converge asymptotically in some sense) and defectors such as fossil fuel producers are interested in delaying the optimum solution. \n" +
            "        </dd> \n" +
            "        <dd>\n" +
            "         Is that nontangential enough? \n" +
            "         <a href=\"/wiki/index.php/Special:Contributions/172.68.143.18\" title=\"Special:Contributions/172.68.143.18\">172.68.143.18</a> 20:49, 26 September 2019 (UTC) \n" +
            "         <dl>\n" +
            "          <dd>\n" +
            "           Yes that was far out&nbsp;:-) I'm sure there is nothing interesting hidden in the image. --\n" +
            "           <a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (\n" +
            "           <a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 08:36, 27 September 2019 (UTC) \n" +
            "           <dl>\n" +
            "            <dd>\n" +
            "             Compare the graph at \n" +
            "             <a rel=\"nofollow\" class=\"external autonumber\" href=\"https://www.wolframalpha.com/input/?i=World+natural+gas+production\">[2]</a> with that at \n" +
            "             <a rel=\"nofollow\" class=\"external autonumber\" href=\"https://www.wolframalpha.com/input/?i=World+wind+power+production\">[3]</a>. When will the latter overtake the former? \n" +
            "             <a href=\"/wiki/index.php/Special:Contributions/172.68.142.221\" title=\"Special:Contributions/172.68.142.221\">172.68.142.221</a> 19:19, 27 September 2019 (UTC) \n" +
            "             <dl>\n" +
            "              <dd>\n" +
            "               Soon one may hope, but that has nothing to do with the drawings on the blackboard...? --\n" +
            "               <a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (\n" +
            "               <a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 21:07, 27 September 2019 (UTC) \n" +
            "               <dl>\n" +
            "                <dd>\n" +
            "                 \"Soon\" lacks mathematical precision. How do you feel about \n" +
            "                 <a href=\"https://en.wikipedia.org/wiki/distributed_constraint_optimization\" class=\"extiw\" title=\"wikipedia:distributed constraint optimization\">distributed constraint optimization</a>? \n" +
            "                 <a href=\"/wiki/index.php/Special:Contributions/172.68.142.83\" title=\"Special:Contributions/172.68.142.83\">172.68.142.83</a> 22:56, 27 September 2019 (UTC)\n" +
            "                </dd> \n" +
            "                <dd>\n" +
            "                 P.S. I would also point out that this comic appeared during the \n" +
            "                 <a rel=\"nofollow\" class=\"external text\" href=\"https://globalclimatestrike.net/\">Global Climate Strike</a> so I stand by my interpretation of the wedges. \n" +
            "                 <a href=\"/wiki/index.php/Special:Contributions/162.158.255.136\" title=\"Special:Contributions/162.158.255.136\">162.158.255.136</a> 19:11, 3 October 2019 (UTC)\n" +
            "                </dd>\n" +
            "               </dl>\n" +
            "              </dd>\n" +
            "             </dl>\n" +
            "            </dd>\n" +
            "           </dl>\n" +
            "          </dd>\n" +
            "         </dl>\n" +
            "        </dd>\n" +
            "       </dl> \n" +
            "       <p>Does <a rel=\"nofollow\" class=\"external text\" href=\"https://www.wolframalpha.com/\">Wolfram Alpha</a> constitute such a problem solver? Cause both Randall and this site has used it on several occasions. But I have not ever really used such things, and do not know if Wolfram can be used as Cueball thinks about in the comic. But if it could, it could be worth mentioning as a method sometimes used by Randall. --<a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (<a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 08:43, 27 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dd>\n" +
            "         <a rel=\"nofollow\" class=\"external autonumber\" href=\"https://www.wolframalpha.com/input/?i=x%2By%3D10%2C+x-y%3D4&amp;lk=3\">[4]</a> is the first bivariate system of equations example. \n" +
            "         <a href=\"/wiki/index.php/Special:Contributions/172.69.22.134\" title=\"Special:Contributions/172.69.22.134\">172.69.22.134</a> 17:51, 27 September 2019 (UTC) \n" +
            "         <dl>\n" +
            "          <dd>\n" +
            "           Is that then a yes to my question?&nbsp;;-) --\n" +
            "           <a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (\n" +
            "           <a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 21:07, 27 September 2019 (UTC) \n" +
            "           <dl>\n" +
            "            <dd>\n" +
            "             Do you think it's more worthwhile to include a general discussion of avoiding the work of solving for two unknowns than the climate wedges? Why do you suggest that the wedges aren't the only distinctive elements on the blackboard? \n" +
            "             <a href=\"/wiki/index.php/Special:Contributions/172.68.142.83\" title=\"Special:Contributions/172.68.142.83\">172.68.142.83</a> 22:58, 27 September 2019 (UTC)\n" +
            "            </dd>\n" +
            "           </dl>\n" +
            "          </dd>\n" +
            "         </dl>\n" +
            "        </dd>\n" +
            "       </dl> \n" +
            "       <p>I only just now noticed that Randall always puts the crossbars on the I in the word \"I\" and not otherwise. Looking back, he has nearly always done this, even since the first few comics. That's quite a principled yet subtle stance on letterforms. (There are some exceptions, however, such as comic #87, and a period that goes at least from comic #128 to comic #180. I wonder if it would be too typography-nerdy to put them all in a category.) <a href=\"/wiki/index.php/Special:Contributions/198.41.231.85\" title=\"Special:Contributions/198.41.231.85\">198.41.231.85</a> 14:47, 27 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dd>\n" +
            "          Those \"crossbars\" would be serifs, whereas he normally uses a sans serif font. A sans serif would be quicker/easier to write by hand, but he probably realized early on (perhaps subconsciously) that an I by itself without serifs looks too much like a random line or a numeral 1 so he treats the solo I like a special letter, with serifs. \n" +
            "         <a href=\"/wiki/index.php/User:N0lqu\" title=\"User:N0lqu\">-boB</a> (\n" +
            "         <a href=\"/wiki/index.php?title=User_talk:N0lqu&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User talk:N0lqu (page does not exist)\">talk</a>) 15:16, 27 September 2019 (UTC) \n" +
            "         <dl>\n" +
            "          <dd>\n" +
            "           Yes so not something for a category! But funny detail. I have no idea where to put this? Maybe in some part of the format of xkcd? --\n" +
            "           <a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (\n" +
            "           <a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 21:07, 27 September 2019 (UTC)\n" +
            "          </dd>\n" +
            "         </dl>\n" +
            "        </dd>\n" +
            "       </dl> \n" +
            "       <p>Thank you, person who sees beauty in grammar (Jkrstrt). I thought something looked off when I said \"often site the beauty they see\" but I didn't catch it until you sighted the error and made it cite instead. <a href=\"/wiki/index.php/User:N0lqu\" title=\"User:N0lqu\">-boB</a> (<a href=\"/wiki/index.php?title=User_talk:N0lqu&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User talk:N0lqu (page does not exist)\">talk</a>) 15:10, 27 September 2019 (UTC) </p>\n" +
            "       <p>We need something about the <a rel=\"nofollow\" class=\"external text\" href=\"https://trends.google.com/trends/explore?date=all&amp;q=%22they%20did%20the%20math%22\">2014 popularity spike of the phrase \"They did the math\"</a> with a link to e.g. r/theydidthemath. And ask the Hashtag Research Studies group to figure out the cause of that spike. <a href=\"/wiki/index.php/Special:Contributions/172.68.189.19\" title=\"Special:Contributions/172.68.189.19\">172.68.189.19</a> 15:29, 29 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dd>\n" +
            "         This has got to be \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://imgur.com/gallery/qpWueVf\">somehow related to xkcd.</a> But how? \n" +
            "         <a href=\"/wiki/index.php/Special:Contributions/172.68.189.19\" title=\"Special:Contributions/172.68.189.19\">172.68.189.19</a> 20:42, 29 September 2019 (UTC)\n" +
            "        </dd>\n" +
            "       </dl> \n" +
            "       <p>In other olds, <a rel=\"nofollow\" class=\"external text\" href=\"https://books.google.com/ngrams/graph?content=they+did+the+math&amp;case_insensitive=on&amp;year_start=1980&amp;year_end=2008&amp;corpus=15&amp;smoothing=3&amp;share=&amp;direct_url=t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0\">Google Books says it started in 1988</a> but won't show me the 1988 book in question. I'm going to work on the drone fishing now. <a href=\"/wiki/index.php/Special:Contributions/162.158.255.136\" title=\"Special:Contributions/162.158.255.136\">162.158.255.136</a> 05:31, 30 September 2019 (UTC) </p> \n" +
            "      </div> \n" +
            "      <!-- \n" +
            "NewPP limit report\n" +
            "Cached time: 20191003191154\n" +
            "Cache expiry: 86400\n" +
            "Dynamic content: false\n" +
            "CPU time usage: 0.133 seconds\n" +
            "Real time usage: 0.151 seconds\n" +
            "Preprocessor visited node count: 521/1000000\n" +
            "Preprocessor generated node count: 1807/1000000\n" +
            "Post‐expand include size: 56659/2097152 bytes\n" +
            "Template argument size: 2599/2097152 bytes\n" +
            "Highest expansion depth: 9/40\n" +
            "Expensive parser function count: 2/100\n" +
            "--> \n" +
            "      <!--\n" +
            "Transclusion expansion time report (%,ms,calls,template)\n" +
            "100.00%   70.288      1 -total\n" +
            " 32.45%   22.811     28 Template:w\n" +
            " 31.87%   22.401      1 Template:comic\n" +
            " 16.32%   11.469      1 Template:comic_discussion\n" +
            " 12.07%    8.482      1 Template:incomplete\n" +
            "  6.57%    4.621      1 Template:notice\n" +
            "  5.66%    3.977      1 Talk:2207:_Math_Work\n" +
            "  2.69%    1.890      2 Template:LATESTCOMIC\n" +
            "  2.23%    1.570      1 MediaWiki:Mainpage\n" +
            "--> \n" +
            "     </div> \n" +
            "     <!-- Saved in parser cache with key db423085716:pcache:idhash:22383-0!canonical and timestamp 20191003191154 and revision id 180723\n" +
            " --> \n" +
            "    </div> \n" +
            "    <div class=\"printfooter\">\n" +
            "      Retrieved from \"\n" +
            "     <a dir=\"ltr\" href=\"https://www.explainxkcd.com/wiki/index.php?title=2207:_Math_Work&amp;oldid=180723\">https://www.explainxkcd.com/wiki/index.php?title=2207:_Math_Work&amp;oldid=180723</a>\" \n" +
            "    </div> \n" +
            "    <div id=\"catlinks\" class=\"catlinks\" data-mw=\"interface\">\n" +
            "     <div id=\"mw-normal-catlinks\" class=\"mw-normal-catlinks\">\n" +
            "      <a href=\"/wiki/index.php/Special:Categories\" title=\"Special:Categories\">Categories</a>: \n" +
            "      <ul>\n" +
            "       <li><a href=\"/wiki/index.php/Category:All_comics\" title=\"Category:All comics\">All comics</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Comics_from_2019\" title=\"Category:Comics from 2019\">Comics from 2019</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Comics_from_September\" title=\"Category:Comics from September\">Comics from September</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Wednesday_comics\" title=\"Category:Wednesday comics\">Wednesday comics</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Incomplete_explanations\" title=\"Category:Incomplete explanations\">Incomplete explanations</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Comics_featuring_White_Hat\" title=\"Category:Comics featuring White Hat\">Comics featuring White Hat</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Comics_featuring_Cueball\" title=\"Category:Comics featuring Cueball\">Comics featuring Cueball</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Physics\" title=\"Category:Physics\">Physics</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Math\" title=\"Category:Math\">Math</a></li>\n" +
            "      </ul>\n" +
            "     </div>\n" +
            "    </div> \n" +
            "    <div class=\"visualClear\"></div> \n" +
            "   </div> \n" +
            "  </div> \n" +
            "  <div id=\"mw-navigation\"> \n" +
            "   <h2>Navigation menu</h2> \n" +
            "   <div id=\"mw-head\"> \n" +
            "    <div id=\"p-personal\" role=\"navigation\" class=\"\" aria-labelledby=\"p-personal-label\"> \n" +
            "     <h3 id=\"p-personal-label\">Personal tools</h3> \n" +
            "     <ul> \n" +
            "      <li id=\"pt-anonuserpage\">Not logged in</li>\n" +
            "      <li id=\"pt-anontalk\"><a href=\"/wiki/index.php/Special:MyTalk\" title=\"Discussion about edits from this IP address [n]\" accesskey=\"n\">Talk</a></li>\n" +
            "      <li id=\"pt-anoncontribs\"><a href=\"/wiki/index.php/Special:MyContributions\" title=\"A list of edits made from this IP address [y]\" accesskey=\"y\">Contributions</a></li>\n" +
            "      <li id=\"pt-createaccount\"><a href=\"/wiki/index.php?title=Special:CreateAccount&amp;returnto=2207%3A+Math+Work\" title=\"You are encouraged to create an account and log in; however, it is not mandatory\">Create account</a></li>\n" +
            "      <li id=\"pt-login\"><a href=\"/wiki/index.php?title=Special:UserLogin&amp;returnto=2207%3A+Math+Work\" title=\"You are encouraged to log in; however, it is not mandatory [o]\" accesskey=\"o\">Log in</a></li> \n" +
            "     </ul> \n" +
            "    </div> \n" +
            "    <div id=\"left-navigation\"> \n" +
            "     <div id=\"p-namespaces\" role=\"navigation\" class=\"vectorTabs\" aria-labelledby=\"p-namespaces-label\"> \n" +
            "      <h3 id=\"p-namespaces-label\">Namespaces</h3> \n" +
            "      <ul> \n" +
            "       <li id=\"ca-nstab-main\" class=\"selected\"><span><a href=\"/wiki/index.php/2207:_Math_Work\" title=\"View the content page [c]\" accesskey=\"c\">Page</a></span></li> \n" +
            "       <li id=\"ca-talk\"><span><a href=\"/wiki/index.php/Talk:2207:_Math_Work\" rel=\"discussion\" title=\"Discussion about the content page [t]\" accesskey=\"t\">Discussion</a></span></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "     <div id=\"p-variants\" role=\"navigation\" class=\"vectorMenu emptyPortlet\" aria-labelledby=\"p-variants-label\"> \n" +
            "      <h3 id=\"p-variants-label\"> <span>Variants</span> </h3> \n" +
            "      <div class=\"menu\"> \n" +
            "       <ul> \n" +
            "       </ul> \n" +
            "      </div> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div id=\"right-navigation\"> \n" +
            "     <div id=\"p-views\" role=\"navigation\" class=\"vectorTabs\" aria-labelledby=\"p-views-label\"> \n" +
            "      <h3 id=\"p-views-label\">Views</h3> \n" +
            "      <ul> \n" +
            "       <li id=\"ca-view\" class=\"selected\"><span><a href=\"/wiki/index.php/2207:_Math_Work\">Read</a></span></li> \n" +
            "       <li id=\"ca-edit\"><span><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit\" title=\"Edit this page [e]\" accesskey=\"e\">Edit</a></span></li> \n" +
            "       <li id=\"ca-history\" class=\"collapsible\"><span><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=history\" title=\"Past revisions of this page [h]\" accesskey=\"h\">View history</a></span></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "     <div id=\"p-cactions\" role=\"navigation\" class=\"vectorMenu emptyPortlet\" aria-labelledby=\"p-cactions-label\"> \n" +
            "      <h3 id=\"p-cactions-label\"><span>More</span></h3> \n" +
            "      <div class=\"menu\"> \n" +
            "       <ul> \n" +
            "       </ul> \n" +
            "      </div> \n" +
            "     </div> \n" +
            "     <div id=\"p-search\" role=\"search\"> \n" +
            "      <h3> <label for=\"searchInput\">Search</label> </h3> \n" +
            "      <form action=\"/wiki/index.php\" id=\"searchform\"> \n" +
            "       <div id=\"simpleSearch\"> \n" +
            "        <input type=\"search\" name=\"search\" placeholder=\"Search explain xkcd\" title=\"Search explain xkcd [f]\" accesskey=\"f\" id=\"searchInput\">\n" +
            "        <input type=\"hidden\" value=\"Special:Search\" name=\"title\">\n" +
            "        <input type=\"submit\" name=\"fulltext\" value=\"Search\" title=\"Search the pages for this text\" id=\"mw-searchButton\" class=\"searchButton mw-fallbackSearchButton\">\n" +
            "        <input type=\"submit\" name=\"go\" value=\"Go\" title=\"Go to a page with this exact name if it exists\" id=\"searchButton\" class=\"searchButton\"> \n" +
            "       </div> \n" +
            "      </form> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "   </div> \n" +
            "   <div id=\"mw-panel\"> \n" +
            "    <div id=\"p-logo\" role=\"banner\">\n" +
            "     <a class=\"mw-wiki-logo\" href=\"/wiki/index.php/Main_Page\" title=\"Visit the main page\"></a>\n" +
            "    </div> \n" +
            "    <div class=\"portal\" role=\"navigation\" id=\"p-navigation\" aria-labelledby=\"p-navigation-label\"> \n" +
            "     <h3 id=\"p-navigation-label\">Navigation</h3> \n" +
            "     <div class=\"body\"> \n" +
            "      <ul> \n" +
            "       <li id=\"n-mainpage-description\"><a href=\"/wiki/index.php/Main_Page\" title=\"Visit the main page [z]\" accesskey=\"z\">Main page</a></li>\n" +
            "       <li id=\"n-Latest-comic\"><a href=\"/wiki/index.php/2210\">Latest comic</a></li>\n" +
            "       <li id=\"n-portal\"><a href=\"/wiki/index.php/explain_xkcd:Community_portal\" title=\"About the project, what you can do, where to find things\">Community portal</a></li>\n" +
            "       <li id=\"n-xkcd-com\"><a href=\"//xkcd.com\" rel=\"nofollow\">xkcd.com</a></li>\n" +
            "       <li id=\"n-recentchanges\"><a href=\"/wiki/index.php/Special:RecentChanges\" title=\"A list of recent changes in the wiki [r]\" accesskey=\"r\">Recent changes</a></li>\n" +
            "       <li id=\"n-randompage\"><a href=\"/wiki/index.php/Special:Random\" title=\"Load a random page [x]\" accesskey=\"x\">Random page</a></li>\n" +
            "       <li id=\"n-All-comics\"><a href=\"/wiki/index.php/List_of_all_comics\">All comics</a></li>\n" +
            "       <li id=\"n-Browse-comics\"><a href=\"/wiki/index.php/Category:Comics\">Browse comics</a></li>\n" +
            "       <li id=\"n-RSS-feed\"><a href=\"//explainxkcd.com/rss.xml\" rel=\"nofollow\">RSS feed</a></li>\n" +
            "       <li id=\"n-help\"><a href=\"https://www.mediawiki.org/wiki/Help:Contents\" title=\"The place to find out\">Help</a></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"portal\" role=\"navigation\" id=\"p-tb\" aria-labelledby=\"p-tb-label\"> \n" +
            "     <h3 id=\"p-tb-label\">Tools</h3> \n" +
            "     <div class=\"body\"> \n" +
            "      <ul> \n" +
            "       <li id=\"t-whatlinkshere\"><a href=\"/wiki/index.php/Special:WhatLinksHere/2207:_Math_Work\" title=\"A list of all wiki pages that link here [j]\" accesskey=\"j\">What links here</a></li>\n" +
            "       <li id=\"t-recentchangeslinked\"><a href=\"/wiki/index.php/Special:RecentChangesLinked/2207:_Math_Work\" rel=\"nofollow\" title=\"Recent changes in pages linked from this page [k]\" accesskey=\"k\">Related changes</a></li>\n" +
            "       <li id=\"t-specialpages\"><a href=\"/wiki/index.php/Special:SpecialPages\" title=\"A list of all special pages [q]\" accesskey=\"q\">Special pages</a></li>\n" +
            "       <li id=\"t-print\"><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;printable=yes\" rel=\"alternate\" title=\"Printable version of this page [p]\" accesskey=\"p\">Printable version</a></li>\n" +
            "       <li id=\"t-permalink\"><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;oldid=180723\" title=\"Permanent link to this revision of the page\">Permanent link</a></li>\n" +
            "       <li id=\"t-info\"><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=info\" title=\"More information about this page\">Page information</a></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"portal\" role=\"navigation\" id=\"p-\" aria-labelledby=\"p--label\"> \n" +
            "     <h3 id=\"p--label\"></h3> \n" +
            "     <div class=\"body\"> \n" +
            "      <div class=\"g-follow\" data-annotation=\"none\" data-height=\"20\" data-href=\"https://plus.google.com/100547197257043990051\" data-rel=\"publisher\"></div> \n" +
            "      <script type=\"text/javascript\">\n" +
            "  (function() {\n" +
            "    var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;\n" +
            "    po.src = 'https://apis.google.com/js/platform.js';\n" +
            "    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);\n" +
            "  })();\n" +
            "</script> \n" +
            "      <a href=\"https://twitter.com/explainxkcd\" class=\"twitter-follow-button\" data-show-count=\"false\" data-show-screen-name=\"false\">Follow @explainxkcd</a> \n" +
            "      <script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');</script> \n" +
            "      <div id=\"fb-root\"></div> \n" +
            "      <script>(function(d, s, id) {\n" +
            "  var js, fjs = d.getElementsByTagName(s)[0];\n" +
            "  if (d.getElementById(id)) return;\n" +
            "  js = d.createElement(s); js.id = id;\n" +
            "  js.src = '//connect.facebook.net/en_US/all.js#xfbml=1';\n" +
            "  fjs.parentNode.insertBefore(js, fjs);\n" +
            "}(document, 'script', 'facebook-jssdk'));</script> \n" +
            "      <div class=\"fb-like\" data-href=\"https://www.facebook.com/explainxkcd\" data-layout=\"button\" data-action=\"like\" data-show-faces=\"false\"></div> \n" +
            "      <style>#pw{position:relative;height:620px;}#lp{position:relative;height:610px;}</style>\n" +
            "      <div id=\"pw\">\n" +
            "       <p></p>\n" +
            "       <div id=\"lp\">\n" +
            "        <a href=\"http://www.lunarpages.com/explainxkcd/\"><img src=\"//www.explainxkcd.com/wiki/lunarpages_160x600.jpg\"></a>\n" +
            "       </div>\n" +
            "      </div> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"portal\" role=\"navigation\" id=\"p-Ads\" aria-labelledby=\"p-Ads-label\"> \n" +
            "     <h3 id=\"p-Ads-label\">Ads</h3> \n" +
            "     <div class=\"body\"> \n" +
            "      <script async src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script> \n" +
            "      <ins class=\"adsbygoogle\" style=\"display:block;\" data-ad-client=\"ca-pub-7040100948805002\" data-ad-format=\"auto\" enable_page_level_ads=\"true\" data-ad-type=\"text\"> </ins> \n" +
            "      <script>\n" +
            "(adsbygoogle = window.adsbygoogle || []).push({});\n" +
            "</script>\n" +
            "      <script>\$('#p-Ads').addClass('persistent');</script> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "   </div> \n" +
            "  </div> \n" +
            "  <div id=\"footer\" role=\"contentinfo\"> \n" +
            "   <ul id=\"footer-info\"> \n" +
            "    <li id=\"footer-info-lastmod\"> This page was last edited on 1 October 2019, at 19:38.</li> \n" +
            "   </ul> \n" +
            "   <ul id=\"footer-places\"> \n" +
            "    <li id=\"footer-places-privacy\"><a href=\"/wiki/index.php/explain_xkcd:Privacy_policy\" title=\"explain xkcd:Privacy policy\">Privacy policy</a></li> \n" +
            "    <li id=\"footer-places-about\"><a href=\"/wiki/index.php/explain_xkcd:About\" class=\"mw-redirect\" title=\"explain xkcd:About\">About explain xkcd</a></li> \n" +
            "    <li id=\"footer-places-disclaimer\"><a href=\"/wiki/index.php/explain_xkcd:General_disclaimer\" title=\"explain xkcd:General disclaimer\">Disclaimers</a></li> \n" +
            "   </ul> \n" +
            "   <ul id=\"footer-icons\" class=\"noprint\"> \n" +
            "    <li id=\"footer-poweredbyico\"> <a href=\"//www.mediawiki.org/\"><img src=\"/wiki/resources/assets/poweredby_mediawiki_88x31.png\" alt=\"Powered by MediaWiki\" srcset=\"/wiki/resources/assets/poweredby_mediawiki_132x47.png 1.5x, /wiki/resources/assets/poweredby_mediawiki_176x62.png 2x\" width=\"88\" height=\"31\"></a><a href=\"https://creativecommons.org/licenses/by-sa/3.0/deed.en_US\"><img src=\"https://i.creativecommons.org/l/by-sa/3.0/88x31.png\" alt=\"Creative Commons License\" width=\"88\" height=\"31\"></a> </li> \n" +
            "   </ul> \n" +
            "   <div style=\"clear:both\"></div> \n" +
            "  </div> \n" +
            "  <script>(window.RLQ=window.RLQ||[]).push(function(){mw.config.set({\"wgPageParseReport\":{\"limitreport\":{\"cputime\":\"0.133\",\"walltime\":\"0.151\",\"ppvisitednodes\":{\"value\":521,\"limit\":1000000},\"ppgeneratednodes\":{\"value\":1807,\"limit\":1000000},\"postexpandincludesize\":{\"value\":56659,\"limit\":2097152},\"templateargumentsize\":{\"value\":2599,\"limit\":2097152},\"expansiondepth\":{\"value\":9,\"limit\":40},\"expensivefunctioncount\":{\"value\":2,\"limit\":100},\"timingprofile\":[\"100.00%   70.288      1 -total\",\" 32.45%   22.811     28 Template:w\",\" 31.87%   22.401      1 Template:comic\",\" 16.32%   11.469      1 Template:comic_discussion\",\" 12.07%    8.482      1 Template:incomplete\",\"  6.57%    4.621      1 Template:notice\",\"  5.66%    3.977      1 Talk:2207:_Math_Work\",\"  2.69%    1.890      2 Template:LATESTCOMIC\",\"  2.23%    1.570      1 MediaWiki:Mainpage\"]},\"cachereport\":{\"timestamp\":\"20191003191154\",\"ttl\":86400,\"transientcontent\":false}}});});</script>\n" +
            "  <script>(window.RLQ=window.RLQ||[]).push(function(){mw.config.set({\"wgBackendResponseTime\":257});});</script>   \n" +
            " </body>\n" +
            "</html>"


    private val resp2207 = "<!doctype html>\n" +
            "<html class=\"client-nojs\" lang=\"en\" dir=\"ltr\">\n" +
            " <head> \n" +
            "  <meta charset=\"UTF-8\"> \n" +
            "  <title>2207: Math Work - explain xkcd</title> \n" +
            "  <script src=\"/cdn-cgi/apps/head/M52ISAAYfDYfNhlAeg3pMasjGfw.js\"></script>\n" +
            "  <script>document.documentElement.className = document.documentElement.className.replace( /(^|\\s)client-nojs(\\s|\$)/, \"\$1client-js\$2\" );</script> \n" +
            "  <script>(window.RLQ=window.RLQ||[]).push(function(){mw.config.set({\"wgCanonicalNamespace\":\"\",\"wgCanonicalSpecialPageName\":false,\"wgNamespaceNumber\":0,\"wgPageName\":\"2207:_Math_Work\",\"wgTitle\":\"2207: Math Work\",\"wgCurRevisionId\":180723,\"wgRevisionId\":180723,\"wgArticleId\":22383,\"wgIsArticle\":true,\"wgIsRedirect\":false,\"wgAction\":\"view\",\"wgUserName\":null,\"wgUserGroups\":[\"*\"],\"wgCategories\":[\"All comics\",\"Comics from 2019\",\"Comics from September\",\"Wednesday comics\",\"Incomplete explanations\",\"Comics featuring White Hat\",\"Comics featuring Cueball\",\"Physics\",\"Math\"],\"wgBreakFrames\":false,\"wgPageContentLanguage\":\"en\",\"wgPageContentModel\":\"wikitext\",\"wgSeparatorTransformTable\":[\"\",\"\"],\"wgDigitTransformTable\":[\"\",\"\"],\"wgDefaultDateFormat\":\"dmy\",\"wgMonthNames\":[\"\",\"January\",\"February\",\"March\",\"April\",\"May\",\"June\",\"July\",\"August\",\"September\",\"October\",\"November\",\"December\"],\"wgMonthNamesShort\":[\"\",\"Jan\",\"Feb\",\"Mar\",\"Apr\",\"May\",\"Jun\",\"Jul\",\"Aug\",\"Sep\",\"Oct\",\"Nov\",\"Dec\"],\"wgRelevantPageName\":\"2207:_Math_Work\",\"wgRelevantArticleId\":22383,\"wgRequestId\":\"a21ab34392336f53728e29fe\",\"wgIsProbablyEditable\":true,\"wgRelevantPageIsProbablyEditable\":true,\"wgRestrictionEdit\":[],\"wgRestrictionMove\":[],\"wgRedirectedFrom\":\"2207\",\"wgInternalRedirectTargetUrl\":\"/wiki/index.php/2207:_Math_Work\"});mw.loader.state({\"site.styles\":\"ready\",\"noscript\":\"ready\",\"user.styles\":\"ready\",\"user\":\"ready\",\"user.options\":\"loading\",\"user.tokens\":\"loading\",\"mediawiki.legacy.shared\":\"ready\",\"mediawiki.legacy.commonPrint\":\"ready\",\"mediawiki.sectionAnchor\":\"ready\",\"mediawiki.skinning.interface\":\"ready\",\"skins.vector.styles\":\"ready\"});mw.loader.implement(\"user.options@0bhc5ha\",function(\$,jQuery,require,module){mw.user.options.set([]);});mw.loader.implement(\"user.tokens@0yp1lfe\",function ( \$, jQuery, require, module ) {\n" +
            "mw.user.tokens.set({\"editToken\":\"+\\\\\",\"patrolToken\":\"+\\\\\",\"watchToken\":\"+\\\\\",\"csrfToken\":\"+\\\\\"});/*@nomin*/\n" +
            "\n" +
            "});mw.loader.load([\"mediawiki.action.view.redirect\",\"site\",\"mediawiki.page.startup\",\"mediawiki.user\",\"mediawiki.hidpi\",\"mediawiki.page.ready\",\"mediawiki.searchSuggest\",\"skins.vector.js\"]);});</script> \n" +
            "  <link rel=\"stylesheet\" href=\"/wiki/load.php?debug=false&amp;lang=en&amp;modules=mediawiki.legacy.commonPrint%2Cshared%7Cmediawiki.sectionAnchor%7Cmediawiki.skinning.interface%7Cskins.vector.styles&amp;only=styles&amp;skin=vector\"> \n" +
            "  <script async src=\"/wiki/load.php?debug=false&amp;lang=en&amp;modules=startup&amp;only=scripts&amp;skin=vector\"></script> \n" +
            "  <meta name=\"ResourceLoaderDynamicStyles\" content=\"\"> \n" +
            "  <link rel=\"stylesheet\" href=\"/wiki/load.php?debug=false&amp;lang=en&amp;modules=site.styles&amp;only=styles&amp;skin=vector\"> \n" +
            "  <meta name=\"generator\" content=\"MediaWiki 1.30.0\"> \n" +
            "  <meta name=\"description\" content=\"Explain xkcd is a wiki dedicated to explaining the webcomic xkcd. Go figure.\"> \n" +
            "  <link rel=\"alternate\" type=\"application/x-wiki\" title=\"Edit\" href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit\"> \n" +
            "  <link rel=\"edit\" title=\"Edit\" href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit\"> \n" +
            "  <link rel=\"shortcut icon\" href=\"/wiki/images/0/04/16px-BlackHat_head.png\"> \n" +
            "  <link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"/wiki/opensearch_desc.php\" title=\"explain xkcd (en)\"> \n" +
            "  <link rel=\"EditURI\" type=\"application/rsd+xml\" href=\"//www.explainxkcd.com/wiki/api.php?action=rsd\"> \n" +
            "  <link rel=\"alternate\" type=\"application/atom+xml\" title=\"explain xkcd Atom feed\" href=\"/wiki/index.php?title=Special:RecentChanges&amp;feed=atom\"> \n" +
            "  <link rel=\"canonical\" href=\"https://www.explainxkcd.com/wiki/index.php/2207:_Math_Work\"> \n" +
            "  <!--[if lt IE 9]><script src=\"/resources/lib/html5shiv/html5shiv.min.js\"></script><![endif]--> \n" +
            " </head> \n" +
            " <body class=\"mediawiki ltr sitedir-ltr mw-hide-empty-elt ns-0 ns-subject page-2207_Math_Work rootpage-2207_Math_Work skin-vector action-view\"> \n" +
            "  <div id=\"mw-page-base\" class=\"noprint\"></div> \n" +
            "  <div id=\"mw-head-base\" class=\"noprint\"></div> \n" +
            "  <div id=\"content\" class=\"mw-body\" role=\"main\"> \n" +
            "   <a id=\"top\"></a> \n" +
            "   <div id=\"siteNotice\" class=\"mw-body-content\">\n" +
            "    <div id=\"mw-dismissablenotice-anonplace\"></div>\n" +
            "    <script>(function(){var node=document.getElementById(\"mw-dismissablenotice-anonplace\");if(node){node.outerHTML=\"\\u003Cdiv id=\\\"localNotice\\\" lang=\\\"en\\\" dir=\\\"ltr\\\"\\u003E\\u003Cdiv class=\\\"mw-parser-output\\\"\\u003E\\u003Cdiv class=\\\"plainlinks\\\" style=\\\"background:#f5faff; border:1px solid #a7d7f9; margin:1em auto 1em auto; width:100%; font-size: 120%; padding: 0.5ex; text-align: center;\\\"\\u003E\\n\\u003Cp\\u003EWe still need to complete some explanations like this one: \\u003Ca href=\\\"/wiki/index.php/1688:_Map_Age_Guide\\\" title=\\\"1688: Map Age Guide\\\"\\u003E1688: Map Age Guide\\u003C/a\\u003E. All incomplete explanations are \\u003Ca href=\\\"/wiki/index.php/Category:Incomplete_explanations\\\" title=\\\"Category:Incomplete explanations\\\"\\u003Ehere\\u003C/a\\u003E.\\n\\u003C/p\\u003E\\u003Cp\\u003ENever use the \\u003Ci\\u003EMath markup language\\u003C/i\\u003E at the transcript. The reason for this you can read at the \\u003Ca href=\\\"/wiki/index.php/explain_xkcd:Editor_FAQ\\\" title=\\\"explain xkcd:Editor FAQ\\\"\\u003EEditor FAQ\\u003C/a\\u003E.\\n\\u003C/p\\u003E\\n\\u003C/div\\u003E\\n\\u003C/div\\u003E\\u003C/div\\u003E\";}}());</script>\n" +
            "   </div> \n" +
            "   <div class=\"mw-indicators mw-body-content\"> \n" +
            "   </div> \n" +
            "   <h1 id=\"firstHeading\" class=\"firstHeading\" lang=\"en\">2207: Math Work</h1> \n" +
            "   <div id=\"bodyContent\" class=\"mw-body-content\"> \n" +
            "    <div id=\"siteSub\" class=\"noprint\">\n" +
            "     Explain xkcd: It's 'cause you're dumb.\n" +
            "    </div> \n" +
            "    <div id=\"contentSub\">\n" +
            "     <span class=\"mw-redirectedfrom\">(Redirected from <a href=\"/wiki/index.php?title=2207&amp;redirect=no\" class=\"mw-redirect\" title=\"2207\">2207</a>)</span>\n" +
            "    </div> \n" +
            "    <div id=\"jump-to-nav\" class=\"mw-jump\">\n" +
            "      Jump to: \n" +
            "     <a href=\"#mw-head\">navigation</a>, \n" +
            "     <a href=\"#p-search\">search</a> \n" +
            "    </div> \n" +
            "    <div id=\"mw-content-text\" lang=\"en\" dir=\"ltr\" class=\"mw-content-ltr\">\n" +
            "     <div class=\"mw-parser-output\">\n" +
            "      <table class=\"\" cellspacing=\"5\" style=\"background-color: #FFFFFF; border: 1px solid #AAAAAA; color: black; font-size: 88%; line-height: 1.5em; margin: 0.5em 0 0.5em 1em; padding: 0.2em; text-align: center; width:98%;\">\n" +
            "       <tbody>\n" +
            "        <tr>\n" +
            "         <td>\n" +
            "          <ul style=\"text-align: center; margin-bottom: 10px;\" class=\"no-link-underline\">\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\"><a href=\"/wiki/index.php/1\" class=\"mw-redirect\" title=\"1\"><span style=\"color: #FFFFFF; padding: 0 12px;\">|&lt;</span></a></li>\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\"><a href=\"/wiki/index.php/2206\" class=\"mw-redirect\" title=\"2206\"><span style=\"color: #FFFFFF; padding: 0 12px;\">&lt;&nbsp;Prev</span></a></li>\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\" class=\"plainlinks\"><a rel=\"nofollow\" class=\"external text\" href=\"https://www.xkcd.com/2207/\"><span style=\"color: #FFFFFF; padding: 0 12px;\">Comic&nbsp;#2207&nbsp;(September&nbsp;25,&nbsp;2019)</span></a></li>\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\"><a href=\"/wiki/index.php/2208\" class=\"mw-redirect\" title=\"2208\"><span style=\"color: #FFFFFF; padding: 0 12px;\">Next&nbsp;&gt;</span></a></li>\n" +
            "           <li style=\"background-color: #6E7B91; border: 1.5px solid #333333; border-radius: 3px 3px 3px 3px; box-shadow: 0 0 5px 0 gray; display: inline; font-size: 16px; font-variant: small-caps; font-weight: 600; margin: 0 4px; padding: 1.5px 0;\"><a href=\"/wiki/index.php/2210\" class=\"mw-redirect\" title=\"2210\"><span style=\"color: #FFFFFF; padding: 0 12px;\">&gt;|</span></a></li>\n" +
            "          </ul></td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "         <td style=\"font-size: 20px; padding-bottom:10px\"><b>Math Work</b></td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "         <td><a href=\"/wiki/index.php/File:math_work.png\" class=\"image\" title=\"I could type this into a solver, which MIGHT help, but would also mean I have to get a lot of parentheses right...\"><img alt=\"I could type this into a solver, which MIGHT help, but would also mean I have to get a lot of parentheses right...\" src=\"/wiki/images/1/14/math_work.png\" width=\"612\" height=\"401\"></a><br><span style=\"\"><span style=\"color:grey\">Title text:</span> I could type this into a solver, which MIGHT help, but would also mean I have to get a lot of parentheses right...</span></td>\n" +
            "        </tr>\n" +
            "       </tbody>\n" +
            "      </table> \n" +
            "      <h2><span class=\"mw-headline\" id=\"Explanation\">Explanation</span><span class=\"mw-editsection\"><span class=\"mw-editsection-bracket\">[</span><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit&amp;section=1\" title=\"Edit section: Explanation\">edit</a><span class=\"mw-editsection-bracket\">]</span></span></h2> \n" +
            "      <table style=\"background-color: white; border: 1px solid #aaa; box-shadow: 2px 2px 2px rgba(0, 0, 0, 0.2); border-left: 10px solid #1E90FF; margin: 0 auto;\" class=\"notice_tpl\"> \n" +
            "       <tbody>\n" +
            "        <tr> \n" +
            "         <td> <img alt=\"Ambox notice.png\" src=\"/wiki/images/c/c8/Ambox_notice.png\" width=\"40\" height=\"40\"> </td> \n" +
            "         <td style=\"padding:0 1em\"> <b>This explanation may be incomplete or incorrect:</b> <i>Created by TWO UNKNOWNS. About half of the explanation seems insufficiently related to the comic. Do NOT delete this tag too soon.</i><br>If you can address this issue, please <b><a rel=\"nofollow\" class=\"external text\" href=\"xkcd://explain.edit\">edit the page</a>!</b> Thanks. </td>\n" +
            "         <br>\n" +
            "         <br>\n" +
            "        </tr>\n" +
            "       </tbody>\n" +
            "      </table>\n" +
            "      <a href=\"https://www.explainxkcd.com/wiki/index.php/White_Hat\" title=\"White Hat\">White Hat</a> is observing a \n" +
            "      <a href=\"https://en.wikipedia.org/wiki/physicist\" class=\"extiw\" title=\"wikipedia:physicist\">physicist</a>, \n" +
            "      <a href=\"https://www.explainxkcd.com/wiki/index.php/Cueball\" title=\"Cueball\">Cueball</a>, who is staring at some (in the comic unreadable) equations and diagrams on a \n" +
            "      <a href=\"https://en.wikipedia.org/wiki/chalkboard\" class=\"extiw\" title=\"wikipedia:chalkboard\">chalkboard</a>. White Hat is neither a physicist nor a \n" +
            "      <a href=\"https://en.wikipedia.org/wiki/mathematician\" class=\"extiw\" title=\"wikipedia:mathematician\">mathematician</a>, and seems to glorify those professions. He wishes he understood Cueball's work and \"the beauty on display here.\" People who profess a love for mathematics often cite the beauty they see in pure math, how things work out so perfectly, as the reason they love math. \n" +
            "      <p>The joke is that Cueball as a physicist is doing something instead quite simple and relatable: Avoiding hard work. Solving many kinds of constraints for two unknowns isn't necessarily difficult, but can be depending on the details. Cueball clearly thinks a solution is possible but would rather find an easier route. The same could be said about the field of mathematics in general: A proof is beautiful to a mathematician when it provides <a href=\"https://en.wikipedia.org/wiki/aesthetic\" class=\"extiw\" title=\"wikipedia:aesthetic\">aesthetic</a> pleasure, usually associated with being easy to understand. A proof is elegant when it is both easy to understand and correct, and mathematical solutions are profound when useful. Record numbers of mathematics interest groups and their forums in which such work is done exist today, from academic journals predating the use of electricity to a plethora of internet math and science fora such as <a href=\"https://en.wikipedia.org/wiki/Wikipedia:Reference_desk/Mathematics\" class=\"extiw\" title=\"wikipedia:Wikipedia:Reference desk/Mathematics\">Wikipedia Reference Desks</a> and Reddit's <a rel=\"nofollow\" class=\"external text\" href=\"https://reddit.com/r/theydidthemath\">/r/theydidthemath</a> forum, which fueled a <a rel=\"nofollow\" class=\"external text\" href=\"https://i.imgur.com/l1r1VEE.png\">resurgence of the phrase \"they did the math\" as a search term in 2014,</a> because it was included in the sidebar of the <a rel=\"nofollow\" class=\"external text\" href=\"https://reddit.com/r/xkcd\">/r/xkcd</a> subreddit, where it remains five years hence, between \"Linguistics\" and \"Ask Historians\" suggesting that the term was popularized by Xkcd fans after <a rel=\"nofollow\" class=\"external text\" href=\"https://books.google.com/ngrams/graph?content=they+did+the+math&amp;case_insensitive=on&amp;year_start=1980&amp;year_end=2008&amp;corpus=15&amp;smoothing=3&amp;share=&amp;direct_url=t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0#t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0\">its initial appearance c. 1988.</a> The proliferation of mathematics fora is certainly also due to the quickly increasing overall level of education and rapidly growing numbers of internet users. </p>\n" +
            "      <p>A mathematical problem involving two unknowns could be a <a href=\"https://en.wikipedia.org/wiki/system_of_linear_equations\" class=\"extiw\" title=\"wikipedia:system of linear equations\">system of linear equations</a> which can often be solved on paper, a blackboard, in a spreadsheet with solver functions, or by a <a href=\"https://en.wikipedia.org/wiki/computer_algebra_system\" class=\"extiw\" title=\"wikipedia:computer algebra system\">computer algebra system</a> such as <a rel=\"nofollow\" class=\"external text\" href=\"https://www.wolframalpha.com/input/?i=x%2By%3D10%2C+x-y%3D4&amp;lk=3\">WolframAlpha.com.</a> Linear equations are a typical kind of more general constraint satisfaction problems, which in turn are <a href=\"https://en.wikipedia.org/wiki/mathematical_optimization\" class=\"extiw\" title=\"wikipedia:mathematical optimization\">mathematical optimization</a> problems, where the minimization of a difference from a goal state (such as that all of the constraining equations are true, for example) indicates the extent to which constraints are met. Sometimes such problem solving activity arises naturally from economic transactions according to, for example, the laws of <a href=\"https://en.wikipedia.org/wiki/supply_and_demand\" class=\"extiw\" title=\"wikipedia:supply and demand\">supply and demand</a>, arising in the general context of civilization and ecology (both of which have properties associated with beauty and mathematical elegance.) Problems solved by economics are examples of <a href=\"https://en.wikipedia.org/wiki/distributed_constraint_optimization\" class=\"extiw\" title=\"wikipedia:distributed constraint optimization\">distributed constraint optimization</a> processes. When economic laws are not sufficiently satisfying constraints, that is a <a href=\"https://en.wikipedia.org/wiki/market_failure\" class=\"extiw\" title=\"wikipedia:market failure\">market failure</a>, which indicates that more artificial and manual mathematical work is required, instead of the naturally arising or otherwise automatic methods contemplated by Cueball. Other distributed constraint optimization systems can be <a href=\"https://en.wikipedia.org/wiki/crowdsourcing\" class=\"extiw\" title=\"wikipedia:crowdsourcing\">crowdsourcing</a> games, such as <a href=\"https://en.wikipedia.org/wiki/FoldIt\" class=\"extiw\" title=\"wikipedia:FoldIt\">FoldIt</a> and <a href=\"https://en.wikipedia.org/wiki/Galaxy_Zoo\" class=\"extiw\" title=\"wikipedia:Galaxy Zoo\">Galaxy Zoo</a>. </p>\n" +
            "      <p>Of the graphic elements on the blackboard, the most distinctive appears to be a pair of wedges from a pie chart, where the radius of the slices is being used to represent another variable than the angles which all pie charts use to represent a primary variable. Since the cartoon is in black and white, the use of color to represent category labels or more variables may be ruled out. Such black-and-white wedges represent two variables, the meaning of which may be unknown to us, let alone their values. The only distributed constraint optimization game which uses such wedges may be the <a href=\"https://en.wikipedia.org/wiki/climate_stabilization_wedge\" class=\"extiw\" title=\"wikipedia:climate stabilization wedge\">climate stabilization wedge</a> game <a rel=\"nofollow\" class=\"external text\" href=\"https://cmi.princeton.edu/wedges/game\">from Princeton University.</a> In that wedge game, angles represent a potential number of gigatons of atmospheric carbon mitigation (out of about 38 for the circle) and radius indicates uptake, or the extent to which the mitigation solution is effective. </p>\n" +
            "      <p>That game is an example of a bivariate optimization problem which might not have to be manually solved by anyone, for example under specific assumptions about the market in <a rel=\"nofollow\" class=\"external text\" href=\"https://x.company/projects/foghorn\">Project Foghorn</a> <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/WlkWabq/ioc-part-1-prototype-article-in-press.pdf\">plants</a> and <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/SRxC3bd/power-to-gas-efficiency.pdf\">power-to-gas upgrades for natural gas</a> power plants. If such market-based approaches to distributed constraint satisfaction are successful, then the work in finding the solution would be performed not entirely by physicists, chemical engineers, mathematicians, or intentional crowdworkers playing a game to achieve the optimal solution(s), but instead in even larger part by far more widely distributed crowdworkers who are simply making their own, ideally self-interested choices regarding their demand for <a href=\"https://en.wikipedia.org/wiki/desalination\" class=\"extiw\" title=\"wikipedia:desalination\">desalinated</a> and <a href=\"https://en.wikipedia.org/wiki/drinking_water\" class=\"extiw\" title=\"wikipedia:drinking water\">potable water</a>, <a href=\"https://en.wikipedia.org/wiki/carbon-neutral_fuel\" class=\"extiw\" title=\"wikipedia:carbon-neutral fuel\">carbon-neutral liquid transportation fuel</a> and carbon-negative <a href=\"https://en.wikipedia.org/wiki/carbon_sequestration\" class=\"extiw\" title=\"wikipedia:carbon sequestration\">sequestration</a> in <a href=\"https://en.wikipedia.org/wiki/fiber-reinforced_composite\" class=\"extiw\" title=\"wikipedia:fiber-reinforced composite\">fiber-reinforced composite</a> lumber, both made from <a href=\"https://en.wikipedia.org/wiki/ocean_acidification\" class=\"extiw\" title=\"wikipedia:ocean acidification\">carbonate dissolved in seawater</a>, and for recycling the carbon in power plant flue exhaust for the <a href=\"https://en.wikipedia.org/wiki/Energy_storage\" class=\"extiw\" title=\"wikipedia:Energy storage\">storage of renewable energy</a> such as off-peak <a href=\"https://en.wikipedia.org/wiki/wind_power\" class=\"extiw\" title=\"wikipedia:wind power\">wind power</a>. The relative beauty, elegance, and simplicity of the possible solutions to such problems are subjective, and might involve strong differences of opinion between outside observers, mathematicians and engineers involved with the details, and <a href=\"https://en.wikipedia.org/wiki/Villain#Sympathetic_villain\" class=\"extiw\" title=\"wikipedia:Villain\">fossil fuel barons</a>, respected and enriched by society for their part in meeting energy demand. (See \"All Chemistry Equations\" in <a href=\"https://www.explainxkcd.com/wiki/index.php/2034:_Equations\" title=\"2034: Equations\">2034: Equations</a>.) Although the original market-focused primary use of <a href=\"https://en.wikipedia.org/wiki/ticker_tape\" class=\"extiw\" title=\"wikipedia:ticker tape\">ticker tape</a> may be a lost art, the economy is still driven by individual free will leveraging self-interested behavior to achieve social gains for civilization. </p>\n" +
            "      <p>The title text continues Cueball's thought process, with the possibility of using an automatic equation solver to find the unknowns. Equation solvers are not often considered beautiful ways to address purely mathematical problems, even if they are often the most efficient and in that sense elegant solutions to applied problems in engineering. Using a formal solver with symbolic, numeric, or both methods requires making sure that the constraints (e.g. equations) are entered correctly, with parentheses balanced in their correct locations for the solution to succeed. While the <a href=\"https://en.wikipedia.org/wiki/mathematical_beauty\" class=\"extiw\" title=\"wikipedia:mathematical beauty\">beauty of mathematics</a> and pure physics may not be associated with automatic solvers in spreadsheets, general optimization methods are considered elegant in applied physics and engineering, with <a rel=\"nofollow\" class=\"external text\" href=\"http://entsphere.com/pub/pdf/1957%20Jaynes,%20ShannonMaxEntBoltzmann.pdf\">Jaynes (1957)</a> cited more than 12,000 times on Google Scholar, including by <a rel=\"nofollow\" class=\"external text\" href=\"https://www.researchgate.net/publication/234147180_Maximum_Entropy_Image_Restoration_in_Astronomy\">a paper cited</a> by the <a rel=\"nofollow\" class=\"external text\" href=\"https://arxiv.org/abs/1711.01286\">first black hole image astronomers</a> for example. </p> \n" +
            "      <h2><span class=\"mw-headline\" id=\"Transcript\">Transcript</span><span class=\"mw-editsection\"><span class=\"mw-editsection-bracket\">[</span><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit&amp;section=2\" title=\"Edit section: Transcript\">edit</a><span class=\"mw-editsection-bracket\">]</span></span></h2> \n" +
            "      <dl>\n" +
            "       <dd>\n" +
            "        [White Hat is watching Cueball from a couple of meters away. Cueball is contemplating the formulas and diagrams that fills the blackboard he stands in front of. Cueball holds a chalk in his hand. None of the content on the blackboard is readable, but there is a diagram in the shape of a circle and a another pie shaped diagram. Both are thinking with large thought bubbles above their heads, with small bubbles connecting them and the larger bubble]\n" +
            "       </dd> \n" +
            "       <dd>\n" +
            "        White Hat (thinking): Amazing watching a physicist at work, exploring universes in a symphony of numbers.\n" +
            "       </dd> \n" +
            "       <dd>\n" +
            "        White Hat (thinking): If only I had studied math, I could appreciate the beauty on display here.\n" +
            "       </dd>\n" +
            "      </dl> \n" +
            "      <dl>\n" +
            "       <dd>\n" +
            "        Cueball (thinking): Oh no. This has \n" +
            "        <i><b>two</b></i> unknowns. That's gonna be really hard.\n" +
            "       </dd> \n" +
            "       <dd>\n" +
            "        Cueball (thinking): Ughhhhhhh.\n" +
            "       </dd> \n" +
            "       <dd>\n" +
            "        Cueball (thinking): \n" +
            "        <i><b>Think.</b></i> There's gotta be a way to avoid doing all that work...\n" +
            "       </dd>\n" +
            "      </dl> \n" +
            "      <p><br> </p> \n" +
            "      <span id=\"Discussion\"></span>\n" +
            "      <span style=\"position:absolute; right:0; padding-top:1em;\"><img alt=\"comment.png\" src=\"/wiki/images/0/03/comment.png\" width=\"16\" height=\"16\">&nbsp;<a rel=\"nofollow\" class=\"external text\" href=\"//www.explainxkcd.com/wiki/index.php?title=Talk:2207:_Math_Work&amp;action=edit\"><b>add a comment!</b></a>&nbsp;⋅&nbsp;<img alt=\"comment.png\" src=\"/wiki/images/0/03/comment.png\" width=\"16\" height=\"16\">&nbsp;<a rel=\"nofollow\" class=\"external text\" href=\"//www.explainxkcd.com/wiki/index.php?title=Talk:2207:_Math_Work&amp;action=edit&amp;section=new\"><b>add a topic (use sparingly)!</b></a>&nbsp;⋅&nbsp;<img alt=\"Icons-mini-action refresh blue.gif\" src=\"/wiki/images/e/e5/Icons-mini-action_refresh_blue.gif\" width=\"16\" height=\"16\">&nbsp;<a rel=\"nofollow\" class=\"external text\" href=\"//www.explainxkcd.com/wiki/index.php?title=2207:_Math_Work&amp;action=purge\"><b>refresh comments!</b></a></span>\n" +
            "      <h1><span class=\"mw-headline\" id=\"Discussion\">Discussion</span></h1>\n" +
            "      <div style=\"border:1px solid grey; background:#eee; padding:1em;\"> \n" +
            "       <p>This makes me think of my profession (software engineer) - Normie: \"Oh wow, that looks complicated!\" Me: wires two pre-existing libraries together and calls it a day <a href=\"/wiki/index.php?title=User:Baldrickk&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User:Baldrickk (page does not exist)\">Baldrickk</a> (<a href=\"/wiki/index.php?title=User_talk:Baldrickk&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User talk:Baldrickk (page does not exist)\">talk</a>) 09:39, 26 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dt>\n" +
            "         Image of Blackboard\n" +
            "        </dt>\n" +
            "       </dl> \n" +
            "       <p>I was looking at the blackboard and was wondering if there were any Easter eggs on it. Here is the result of my badly cropped photoshopping skills. <a rel=\"nofollow\" class=\"external autonumber\" href=\"https://drive.google.com/open?id=1kGCrQehNGksE2cSK1WvTJcgdwaZ5cdWe\">[1]</a> idk if it would help to sharpen the image. --<a href=\"/wiki/index.php?title=User:DarkAndromeda31&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User:DarkAndromeda31 (page does not exist)\">DarkAndromeda31</a> (<a href=\"/wiki/index.php?title=User_talk:DarkAndromeda31&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User talk:DarkAndromeda31 (page does not exist)\">talk</a>) 01:25, 26 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dd>\n" +
            "         The only thing that really jumps out at me are the wedges, as portions of pie charts where radius also controls area, evoking the \n" +
            "         <a href=\"https://en.wikipedia.org/wiki/climate_stabilization_wedge\" class=\"extiw\" title=\"wikipedia:climate stabilization wedge\">climate stabilization wedge</a> game \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://cmi.princeton.edu/wedges/game\">from Princeton</a> where the total area of the disk needing to be mitigated is something like 38 gigatons of atmospheric carbon, and the various mitigation solutions have angles representing potential and radius indicating uptake, the proportion of which represents gigatons mitigated as the wedge area. We can offer that game as an example of a bivariate optimization problem which might not have to be manually solved by anyone, if we assume that the local market for \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://drive.google.com/file/d/1ritJrcDKyXNe4Kp2dHBWiFuyBEHvn_81/view\">surplus potable water, carbon-neutral liquid transportation fuel, and carbon-negative composite lumber for centuries-to-millenia scale sequestration along with wood timber displacement for reforestation</a> represents locally satisfiable economic demand for N shipping containers of \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://x.company/projects/foghorn\">Project Foghorn</a> \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/WlkWabq/ioc-part-1-prototype-article-in-press.pdf\">plants</a> and M shipping containers of \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/SRxC3bd/power-to-gas-efficiency.pdf\">power-to-gas upgrades for natural gas</a> power plants. That's an example of how a locally market-driven system can solve a bivariate optimization without anyone doing the actual math work in a spreadsheet or otherwise. The economic solution is not necessarily optimal, because even \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://twitter.com/jsalsman/status/1118030378747351040\">as powerful as the free market can be,</a> it isn't necessarily going to find the bivariate optimums for every point on the planet (although it will likely converge asymptotically in some sense) and defectors such as fossil fuel producers are interested in delaying the optimum solution. \n" +
            "        </dd> \n" +
            "        <dd>\n" +
            "         Is that nontangential enough? \n" +
            "         <a href=\"/wiki/index.php/Special:Contributions/172.68.143.18\" title=\"Special:Contributions/172.68.143.18\">172.68.143.18</a> 20:49, 26 September 2019 (UTC) \n" +
            "         <dl>\n" +
            "          <dd>\n" +
            "           Yes that was far out&nbsp;:-) I'm sure there is nothing interesting hidden in the image. --\n" +
            "           <a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (\n" +
            "           <a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 08:36, 27 September 2019 (UTC) \n" +
            "           <dl>\n" +
            "            <dd>\n" +
            "             Compare the graph at \n" +
            "             <a rel=\"nofollow\" class=\"external autonumber\" href=\"https://www.wolframalpha.com/input/?i=World+natural+gas+production\">[2]</a> with that at \n" +
            "             <a rel=\"nofollow\" class=\"external autonumber\" href=\"https://www.wolframalpha.com/input/?i=World+wind+power+production\">[3]</a>. When will the latter overtake the former? \n" +
            "             <a href=\"/wiki/index.php/Special:Contributions/172.68.142.221\" title=\"Special:Contributions/172.68.142.221\">172.68.142.221</a> 19:19, 27 September 2019 (UTC) \n" +
            "             <dl>\n" +
            "              <dd>\n" +
            "               Soon one may hope, but that has nothing to do with the drawings on the blackboard...? --\n" +
            "               <a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (\n" +
            "               <a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 21:07, 27 September 2019 (UTC) \n" +
            "               <dl>\n" +
            "                <dd>\n" +
            "                 \"Soon\" lacks mathematical precision. How do you feel about \n" +
            "                 <a href=\"https://en.wikipedia.org/wiki/distributed_constraint_optimization\" class=\"extiw\" title=\"wikipedia:distributed constraint optimization\">distributed constraint optimization</a>? \n" +
            "                 <a href=\"/wiki/index.php/Special:Contributions/172.68.142.83\" title=\"Special:Contributions/172.68.142.83\">172.68.142.83</a> 22:56, 27 September 2019 (UTC)\n" +
            "                </dd> \n" +
            "                <dd>\n" +
            "                 P.S. I would also point out that this comic appeared during the \n" +
            "                 <a rel=\"nofollow\" class=\"external text\" href=\"https://globalclimatestrike.net/\">Global Climate Strike</a> so I stand by my interpretation of the wedges. \n" +
            "                 <a href=\"/wiki/index.php/Special:Contributions/162.158.255.136\" title=\"Special:Contributions/162.158.255.136\">162.158.255.136</a> 19:11, 3 October 2019 (UTC)\n" +
            "                </dd>\n" +
            "               </dl>\n" +
            "              </dd>\n" +
            "             </dl>\n" +
            "            </dd>\n" +
            "           </dl>\n" +
            "          </dd>\n" +
            "         </dl>\n" +
            "        </dd>\n" +
            "       </dl> \n" +
            "       <p>Does <a rel=\"nofollow\" class=\"external text\" href=\"https://www.wolframalpha.com/\">Wolfram Alpha</a> constitute such a problem solver? Cause both Randall and this site has used it on several occasions. But I have not ever really used such things, and do not know if Wolfram can be used as Cueball thinks about in the comic. But if it could, it could be worth mentioning as a method sometimes used by Randall. --<a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (<a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 08:43, 27 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dd>\n" +
            "         <a rel=\"nofollow\" class=\"external autonumber\" href=\"https://www.wolframalpha.com/input/?i=x%2By%3D10%2C+x-y%3D4&amp;lk=3\">[4]</a> is the first bivariate system of equations example. \n" +
            "         <a href=\"/wiki/index.php/Special:Contributions/172.69.22.134\" title=\"Special:Contributions/172.69.22.134\">172.69.22.134</a> 17:51, 27 September 2019 (UTC) \n" +
            "         <dl>\n" +
            "          <dd>\n" +
            "           Is that then a yes to my question?&nbsp;;-) --\n" +
            "           <a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (\n" +
            "           <a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 21:07, 27 September 2019 (UTC) \n" +
            "           <dl>\n" +
            "            <dd>\n" +
            "             Do you think it's more worthwhile to include a general discussion of avoiding the work of solving for two unknowns than the climate wedges? Why do you suggest that the wedges aren't the only distinctive elements on the blackboard? \n" +
            "             <a href=\"/wiki/index.php/Special:Contributions/172.68.142.83\" title=\"Special:Contributions/172.68.142.83\">172.68.142.83</a> 22:58, 27 September 2019 (UTC)\n" +
            "            </dd>\n" +
            "           </dl>\n" +
            "          </dd>\n" +
            "         </dl>\n" +
            "        </dd>\n" +
            "       </dl> \n" +
            "       <p>I only just now noticed that Randall always puts the crossbars on the I in the word \"I\" and not otherwise. Looking back, he has nearly always done this, even since the first few comics. That's quite a principled yet subtle stance on letterforms. (There are some exceptions, however, such as comic #87, and a period that goes at least from comic #128 to comic #180. I wonder if it would be too typography-nerdy to put them all in a category.) <a href=\"/wiki/index.php/Special:Contributions/198.41.231.85\" title=\"Special:Contributions/198.41.231.85\">198.41.231.85</a> 14:47, 27 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dd>\n" +
            "          Those \"crossbars\" would be serifs, whereas he normally uses a sans serif font. A sans serif would be quicker/easier to write by hand, but he probably realized early on (perhaps subconsciously) that an I by itself without serifs looks too much like a random line or a numeral 1 so he treats the solo I like a special letter, with serifs. \n" +
            "         <a href=\"/wiki/index.php/User:N0lqu\" title=\"User:N0lqu\">-boB</a> (\n" +
            "         <a href=\"/wiki/index.php?title=User_talk:N0lqu&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User talk:N0lqu (page does not exist)\">talk</a>) 15:16, 27 September 2019 (UTC) \n" +
            "         <dl>\n" +
            "          <dd>\n" +
            "           Yes so not something for a category! But funny detail. I have no idea where to put this? Maybe in some part of the format of xkcd? --\n" +
            "           <a href=\"/wiki/index.php/User:Kynde\" title=\"User:Kynde\">Kynde</a> (\n" +
            "           <a href=\"/wiki/index.php/User_talk:Kynde\" title=\"User talk:Kynde\">talk</a>) 21:07, 27 September 2019 (UTC)\n" +
            "          </dd>\n" +
            "         </dl>\n" +
            "        </dd>\n" +
            "       </dl> \n" +
            "       <p>Thank you, person who sees beauty in grammar (Jkrstrt). I thought something looked off when I said \"often site the beauty they see\" but I didn't catch it until you sighted the error and made it cite instead. <a href=\"/wiki/index.php/User:N0lqu\" title=\"User:N0lqu\">-boB</a> (<a href=\"/wiki/index.php?title=User_talk:N0lqu&amp;action=edit&amp;redlink=1\" class=\"new\" title=\"User talk:N0lqu (page does not exist)\">talk</a>) 15:10, 27 September 2019 (UTC) </p>\n" +
            "       <p>We need something about the <a rel=\"nofollow\" class=\"external text\" href=\"https://trends.google.com/trends/explore?date=all&amp;q=%22they%20did%20the%20math%22\">2014 popularity spike of the phrase \"They did the math\"</a> with a link to e.g. r/theydidthemath. And ask the Hashtag Research Studies group to figure out the cause of that spike. <a href=\"/wiki/index.php/Special:Contributions/172.68.189.19\" title=\"Special:Contributions/172.68.189.19\">172.68.189.19</a> 15:29, 29 September 2019 (UTC) </p> \n" +
            "       <dl>\n" +
            "        <dd>\n" +
            "         This has got to be \n" +
            "         <a rel=\"nofollow\" class=\"external text\" href=\"https://imgur.com/gallery/qpWueVf\">somehow related to xkcd.</a> But how? \n" +
            "         <a href=\"/wiki/index.php/Special:Contributions/172.68.189.19\" title=\"Special:Contributions/172.68.189.19\">172.68.189.19</a> 20:42, 29 September 2019 (UTC)\n" +
            "        </dd>\n" +
            "       </dl> \n" +
            "       <p>In other olds, <a rel=\"nofollow\" class=\"external text\" href=\"https://books.google.com/ngrams/graph?content=they+did+the+math&amp;case_insensitive=on&amp;year_start=1980&amp;year_end=2008&amp;corpus=15&amp;smoothing=3&amp;share=&amp;direct_url=t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0\">Google Books says it started in 1988</a> but won't show me the 1988 book in question. I'm going to work on the drone fishing now. <a href=\"/wiki/index.php/Special:Contributions/162.158.255.136\" title=\"Special:Contributions/162.158.255.136\">162.158.255.136</a> 05:31, 30 September 2019 (UTC) </p> \n" +
            "      </div> \n" +
            "      <!-- \n" +
            "NewPP limit report\n" +
            "Cached time: 20191003191154\n" +
            "Cache expiry: 86400\n" +
            "Dynamic content: false\n" +
            "CPU time usage: 0.133 seconds\n" +
            "Real time usage: 0.151 seconds\n" +
            "Preprocessor visited node count: 521/1000000\n" +
            "Preprocessor generated node count: 1807/1000000\n" +
            "Post‐expand include size: 56659/2097152 bytes\n" +
            "Template argument size: 2599/2097152 bytes\n" +
            "Highest expansion depth: 9/40\n" +
            "Expensive parser function count: 2/100\n" +
            "--> \n" +
            "      <!--\n" +
            "Transclusion expansion time report (%,ms,calls,template)\n" +
            "100.00%   70.288      1 -total\n" +
            " 32.45%   22.811     28 Template:w\n" +
            " 31.87%   22.401      1 Template:comic\n" +
            " 16.32%   11.469      1 Template:comic_discussion\n" +
            " 12.07%    8.482      1 Template:incomplete\n" +
            "  6.57%    4.621      1 Template:notice\n" +
            "  5.66%    3.977      1 Talk:2207:_Math_Work\n" +
            "  2.69%    1.890      2 Template:LATESTCOMIC\n" +
            "  2.23%    1.570      1 MediaWiki:Mainpage\n" +
            "--> \n" +
            "     </div> \n" +
            "     <!-- Saved in parser cache with key db423085716:pcache:idhash:22383-0!canonical and timestamp 20191003191154 and revision id 180723\n" +
            " --> \n" +
            "    </div> \n" +
            "    <div class=\"printfooter\">\n" +
            "      Retrieved from \"\n" +
            "     <a dir=\"ltr\" href=\"https://www.explainxkcd.com/wiki/index.php?title=2207:_Math_Work&amp;oldid=180723\">https://www.explainxkcd.com/wiki/index.php?title=2207:_Math_Work&amp;oldid=180723</a>\" \n" +
            "    </div> \n" +
            "    <div id=\"catlinks\" class=\"catlinks\" data-mw=\"interface\">\n" +
            "     <div id=\"mw-normal-catlinks\" class=\"mw-normal-catlinks\">\n" +
            "      <a href=\"/wiki/index.php/Special:Categories\" title=\"Special:Categories\">Categories</a>: \n" +
            "      <ul>\n" +
            "       <li><a href=\"/wiki/index.php/Category:All_comics\" title=\"Category:All comics\">All comics</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Comics_from_2019\" title=\"Category:Comics from 2019\">Comics from 2019</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Comics_from_September\" title=\"Category:Comics from September\">Comics from September</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Wednesday_comics\" title=\"Category:Wednesday comics\">Wednesday comics</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Incomplete_explanations\" title=\"Category:Incomplete explanations\">Incomplete explanations</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Comics_featuring_White_Hat\" title=\"Category:Comics featuring White Hat\">Comics featuring White Hat</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Comics_featuring_Cueball\" title=\"Category:Comics featuring Cueball\">Comics featuring Cueball</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Physics\" title=\"Category:Physics\">Physics</a></li>\n" +
            "       <li><a href=\"/wiki/index.php/Category:Math\" title=\"Category:Math\">Math</a></li>\n" +
            "      </ul>\n" +
            "     </div>\n" +
            "    </div> \n" +
            "    <div class=\"visualClear\"></div> \n" +
            "   </div> \n" +
            "  </div> \n" +
            "  <div id=\"mw-navigation\"> \n" +
            "   <h2>Navigation menu</h2> \n" +
            "   <div id=\"mw-head\"> \n" +
            "    <div id=\"p-personal\" role=\"navigation\" class=\"\" aria-labelledby=\"p-personal-label\"> \n" +
            "     <h3 id=\"p-personal-label\">Personal tools</h3> \n" +
            "     <ul> \n" +
            "      <li id=\"pt-anonuserpage\">Not logged in</li>\n" +
            "      <li id=\"pt-anontalk\"><a href=\"/wiki/index.php/Special:MyTalk\" title=\"Discussion about edits from this IP address [n]\" accesskey=\"n\">Talk</a></li>\n" +
            "      <li id=\"pt-anoncontribs\"><a href=\"/wiki/index.php/Special:MyContributions\" title=\"A list of edits made from this IP address [y]\" accesskey=\"y\">Contributions</a></li>\n" +
            "      <li id=\"pt-createaccount\"><a href=\"/wiki/index.php?title=Special:CreateAccount&amp;returnto=2207%3A+Math+Work\" title=\"You are encouraged to create an account and log in; however, it is not mandatory\">Create account</a></li>\n" +
            "      <li id=\"pt-login\"><a href=\"/wiki/index.php?title=Special:UserLogin&amp;returnto=2207%3A+Math+Work\" title=\"You are encouraged to log in; however, it is not mandatory [o]\" accesskey=\"o\">Log in</a></li> \n" +
            "     </ul> \n" +
            "    </div> \n" +
            "    <div id=\"left-navigation\"> \n" +
            "     <div id=\"p-namespaces\" role=\"navigation\" class=\"vectorTabs\" aria-labelledby=\"p-namespaces-label\"> \n" +
            "      <h3 id=\"p-namespaces-label\">Namespaces</h3> \n" +
            "      <ul> \n" +
            "       <li id=\"ca-nstab-main\" class=\"selected\"><span><a href=\"/wiki/index.php/2207:_Math_Work\" title=\"View the content page [c]\" accesskey=\"c\">Page</a></span></li> \n" +
            "       <li id=\"ca-talk\"><span><a href=\"/wiki/index.php/Talk:2207:_Math_Work\" rel=\"discussion\" title=\"Discussion about the content page [t]\" accesskey=\"t\">Discussion</a></span></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "     <div id=\"p-variants\" role=\"navigation\" class=\"vectorMenu emptyPortlet\" aria-labelledby=\"p-variants-label\"> \n" +
            "      <h3 id=\"p-variants-label\"> <span>Variants</span> </h3> \n" +
            "      <div class=\"menu\"> \n" +
            "       <ul> \n" +
            "       </ul> \n" +
            "      </div> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div id=\"right-navigation\"> \n" +
            "     <div id=\"p-views\" role=\"navigation\" class=\"vectorTabs\" aria-labelledby=\"p-views-label\"> \n" +
            "      <h3 id=\"p-views-label\">Views</h3> \n" +
            "      <ul> \n" +
            "       <li id=\"ca-view\" class=\"selected\"><span><a href=\"/wiki/index.php/2207:_Math_Work\">Read</a></span></li> \n" +
            "       <li id=\"ca-edit\"><span><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=edit\" title=\"Edit this page [e]\" accesskey=\"e\">Edit</a></span></li> \n" +
            "       <li id=\"ca-history\" class=\"collapsible\"><span><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=history\" title=\"Past revisions of this page [h]\" accesskey=\"h\">View history</a></span></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "     <div id=\"p-cactions\" role=\"navigation\" class=\"vectorMenu emptyPortlet\" aria-labelledby=\"p-cactions-label\"> \n" +
            "      <h3 id=\"p-cactions-label\"><span>More</span></h3> \n" +
            "      <div class=\"menu\"> \n" +
            "       <ul> \n" +
            "       </ul> \n" +
            "      </div> \n" +
            "     </div> \n" +
            "     <div id=\"p-search\" role=\"search\"> \n" +
            "      <h3> <label for=\"searchInput\">Search</label> </h3> \n" +
            "      <form action=\"/wiki/index.php\" id=\"searchform\"> \n" +
            "       <div id=\"simpleSearch\"> \n" +
            "        <input type=\"search\" name=\"search\" placeholder=\"Search explain xkcd\" title=\"Search explain xkcd [f]\" accesskey=\"f\" id=\"searchInput\">\n" +
            "        <input type=\"hidden\" value=\"Special:Search\" name=\"title\">\n" +
            "        <input type=\"submit\" name=\"fulltext\" value=\"Search\" title=\"Search the pages for this text\" id=\"mw-searchButton\" class=\"searchButton mw-fallbackSearchButton\">\n" +
            "        <input type=\"submit\" name=\"go\" value=\"Go\" title=\"Go to a page with this exact name if it exists\" id=\"searchButton\" class=\"searchButton\"> \n" +
            "       </div> \n" +
            "      </form> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "   </div> \n" +
            "   <div id=\"mw-panel\"> \n" +
            "    <div id=\"p-logo\" role=\"banner\">\n" +
            "     <a class=\"mw-wiki-logo\" href=\"/wiki/index.php/Main_Page\" title=\"Visit the main page\"></a>\n" +
            "    </div> \n" +
            "    <div class=\"portal\" role=\"navigation\" id=\"p-navigation\" aria-labelledby=\"p-navigation-label\"> \n" +
            "     <h3 id=\"p-navigation-label\">Navigation</h3> \n" +
            "     <div class=\"body\"> \n" +
            "      <ul> \n" +
            "       <li id=\"n-mainpage-description\"><a href=\"/wiki/index.php/Main_Page\" title=\"Visit the main page [z]\" accesskey=\"z\">Main page</a></li>\n" +
            "       <li id=\"n-Latest-comic\"><a href=\"/wiki/index.php/2210\">Latest comic</a></li>\n" +
            "       <li id=\"n-portal\"><a href=\"/wiki/index.php/explain_xkcd:Community_portal\" title=\"About the project, what you can do, where to find things\">Community portal</a></li>\n" +
            "       <li id=\"n-xkcd-com\"><a href=\"//xkcd.com\" rel=\"nofollow\">xkcd.com</a></li>\n" +
            "       <li id=\"n-recentchanges\"><a href=\"/wiki/index.php/Special:RecentChanges\" title=\"A list of recent changes in the wiki [r]\" accesskey=\"r\">Recent changes</a></li>\n" +
            "       <li id=\"n-randompage\"><a href=\"/wiki/index.php/Special:Random\" title=\"Load a random page [x]\" accesskey=\"x\">Random page</a></li>\n" +
            "       <li id=\"n-All-comics\"><a href=\"/wiki/index.php/List_of_all_comics\">All comics</a></li>\n" +
            "       <li id=\"n-Browse-comics\"><a href=\"/wiki/index.php/Category:Comics\">Browse comics</a></li>\n" +
            "       <li id=\"n-RSS-feed\"><a href=\"//explainxkcd.com/rss.xml\" rel=\"nofollow\">RSS feed</a></li>\n" +
            "       <li id=\"n-help\"><a href=\"https://www.mediawiki.org/wiki/Help:Contents\" title=\"The place to find out\">Help</a></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"portal\" role=\"navigation\" id=\"p-tb\" aria-labelledby=\"p-tb-label\"> \n" +
            "     <h3 id=\"p-tb-label\">Tools</h3> \n" +
            "     <div class=\"body\"> \n" +
            "      <ul> \n" +
            "       <li id=\"t-whatlinkshere\"><a href=\"/wiki/index.php/Special:WhatLinksHere/2207:_Math_Work\" title=\"A list of all wiki pages that link here [j]\" accesskey=\"j\">What links here</a></li>\n" +
            "       <li id=\"t-recentchangeslinked\"><a href=\"/wiki/index.php/Special:RecentChangesLinked/2207:_Math_Work\" rel=\"nofollow\" title=\"Recent changes in pages linked from this page [k]\" accesskey=\"k\">Related changes</a></li>\n" +
            "       <li id=\"t-specialpages\"><a href=\"/wiki/index.php/Special:SpecialPages\" title=\"A list of all special pages [q]\" accesskey=\"q\">Special pages</a></li>\n" +
            "       <li id=\"t-print\"><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;printable=yes\" rel=\"alternate\" title=\"Printable version of this page [p]\" accesskey=\"p\">Printable version</a></li>\n" +
            "       <li id=\"t-permalink\"><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;oldid=180723\" title=\"Permanent link to this revision of the page\">Permanent link</a></li>\n" +
            "       <li id=\"t-info\"><a href=\"/wiki/index.php?title=2207:_Math_Work&amp;action=info\" title=\"More information about this page\">Page information</a></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"portal\" role=\"navigation\" id=\"p-\" aria-labelledby=\"p--label\"> \n" +
            "     <h3 id=\"p--label\"></h3> \n" +
            "     <div class=\"body\"> \n" +
            "      <div class=\"g-follow\" data-annotation=\"none\" data-height=\"20\" data-href=\"https://plus.google.com/100547197257043990051\" data-rel=\"publisher\"></div> \n" +
            "      <script type=\"text/javascript\">\n" +
            "  (function() {\n" +
            "    var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;\n" +
            "    po.src = 'https://apis.google.com/js/platform.js';\n" +
            "    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);\n" +
            "  })();\n" +
            "</script> \n" +
            "      <a href=\"https://twitter.com/explainxkcd\" class=\"twitter-follow-button\" data-show-count=\"false\" data-show-screen-name=\"false\">Follow @explainxkcd</a> \n" +
            "      <script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');</script> \n" +
            "      <div id=\"fb-root\"></div> \n" +
            "      <script>(function(d, s, id) {\n" +
            "  var js, fjs = d.getElementsByTagName(s)[0];\n" +
            "  if (d.getElementById(id)) return;\n" +
            "  js = d.createElement(s); js.id = id;\n" +
            "  js.src = '//connect.facebook.net/en_US/all.js#xfbml=1';\n" +
            "  fjs.parentNode.insertBefore(js, fjs);\n" +
            "}(document, 'script', 'facebook-jssdk'));</script> \n" +
            "      <div class=\"fb-like\" data-href=\"https://www.facebook.com/explainxkcd\" data-layout=\"button\" data-action=\"like\" data-show-faces=\"false\"></div> \n" +
            "      <style>#pw{position:relative;height:620px;}#lp{position:relative;height:610px;}</style>\n" +
            "      <div id=\"pw\">\n" +
            "       <p></p>\n" +
            "       <div id=\"lp\">\n" +
            "        <a href=\"http://www.lunarpages.com/explainxkcd/\"><img src=\"//www.explainxkcd.com/wiki/lunarpages_160x600.jpg\"></a>\n" +
            "       </div>\n" +
            "      </div> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"portal\" role=\"navigation\" id=\"p-Ads\" aria-labelledby=\"p-Ads-label\"> \n" +
            "     <h3 id=\"p-Ads-label\">Ads</h3> \n" +
            "     <div class=\"body\"> \n" +
            "      <script async src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script> \n" +
            "      <ins class=\"adsbygoogle\" style=\"display:block;\" data-ad-client=\"ca-pub-7040100948805002\" data-ad-format=\"auto\" enable_page_level_ads=\"true\" data-ad-type=\"text\"> </ins> \n" +
            "      <script>\n" +
            "(adsbygoogle = window.adsbygoogle || []).push({});\n" +
            "</script>\n" +
            "      <script>\$('#p-Ads').addClass('persistent');</script> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "   </div> \n" +
            "  </div> \n" +
            "  <div id=\"footer\" role=\"contentinfo\"> \n" +
            "   <ul id=\"footer-info\"> \n" +
            "    <li id=\"footer-info-lastmod\"> This page was last edited on 1 October 2019, at 19:38.</li> \n" +
            "   </ul> \n" +
            "   <ul id=\"footer-places\"> \n" +
            "    <li id=\"footer-places-privacy\"><a href=\"/wiki/index.php/explain_xkcd:Privacy_policy\" title=\"explain xkcd:Privacy policy\">Privacy policy</a></li> \n" +
            "    <li id=\"footer-places-about\"><a href=\"/wiki/index.php/explain_xkcd:About\" class=\"mw-redirect\" title=\"explain xkcd:About\">About explain xkcd</a></li> \n" +
            "    <li id=\"footer-places-disclaimer\"><a href=\"/wiki/index.php/explain_xkcd:General_disclaimer\" title=\"explain xkcd:General disclaimer\">Disclaimers</a></li> \n" +
            "   </ul> \n" +
            "   <ul id=\"footer-icons\" class=\"noprint\"> \n" +
            "    <li id=\"footer-poweredbyico\"> <a href=\"//www.mediawiki.org/\"><img src=\"/wiki/resources/assets/poweredby_mediawiki_88x31.png\" alt=\"Powered by MediaWiki\" srcset=\"/wiki/resources/assets/poweredby_mediawiki_132x47.png 1.5x, /wiki/resources/assets/poweredby_mediawiki_176x62.png 2x\" width=\"88\" height=\"31\"></a><a href=\"https://creativecommons.org/licenses/by-sa/3.0/deed.en_US\"><img src=\"https://i.creativecommons.org/l/by-sa/3.0/88x31.png\" alt=\"Creative Commons License\" width=\"88\" height=\"31\"></a> </li> \n" +
            "   </ul> \n" +
            "   <div style=\"clear:both\"></div> \n" +
            "  </div> \n" +
            "  <script>(window.RLQ=window.RLQ||[]).push(function(){mw.config.set({\"wgPageParseReport\":{\"limitreport\":{\"cputime\":\"0.133\",\"walltime\":\"0.151\",\"ppvisitednodes\":{\"value\":521,\"limit\":1000000},\"ppgeneratednodes\":{\"value\":1807,\"limit\":1000000},\"postexpandincludesize\":{\"value\":56659,\"limit\":2097152},\"templateargumentsize\":{\"value\":2599,\"limit\":2097152},\"expansiondepth\":{\"value\":9,\"limit\":40},\"expensivefunctioncount\":{\"value\":2,\"limit\":100},\"timingprofile\":[\"100.00%   70.288      1 -total\",\" 32.45%   22.811     28 Template:w\",\" 31.87%   22.401      1 Template:comic\",\" 16.32%   11.469      1 Template:comic_discussion\",\" 12.07%    8.482      1 Template:incomplete\",\"  6.57%    4.621      1 Template:notice\",\"  5.66%    3.977      1 Talk:2207:_Math_Work\",\"  2.69%    1.890      2 Template:LATESTCOMIC\",\"  2.23%    1.570      1 MediaWiki:Mainpage\"]},\"cachereport\":{\"timestamp\":\"20191003191154\",\"ttl\":86400,\"transientcontent\":false}}});});</script>\n" +
            "  <script>(window.RLQ=window.RLQ||[]).push(function(){mw.config.set({\"wgBackendResponseTime\":257});});</script>   \n" +
            " </body>\n" +
            "</html>"

    private val result2207 = " <br><br><table style=\"background-color: white; border: 1px solid #aaa; box-shadow: 2px 2px 2px rgba(0, 0, 0, 0.2); border-left: 10px solid #1E90FF; margin: 0 auto;\" class=\"notice_tpl\"> \n" +
            " <tbody>\n" +
            "  <tr> \n" +
            "   <td> <img alt=\"Ambox notice.png\" src=\"/wiki/images/c/c8/Ambox_notice.png\" width=\"40\" height=\"40\"> </td> \n" +
            "   <td style=\"padding:0 1em\"> <b>This explanation may be incomplete or incorrect:</b> <i>Created by TWO UNKNOWNS. About half of the explanation seems insufficiently related to the comic. Do NOT delete this tag too soon.</i><br>If you can address this issue, please <b><a rel=\"nofollow\" class=\"external text\" href=\"xkcd://explain.edit\">edit the page</a>!</b> Thanks. </td>\n" +
            "   <br>\n" +
            "   <br>\n" +
            "  </tr>\n" +
            " </tbody>\n" +
            "</table><a href=\"https://www.explainxkcd.com/wiki/index.php/White_Hat\" title=\"White Hat\">White Hat</a> is observing a <a href=\"https://en.wikipedia.org/wiki/physicist\" class=\"extiw\" title=\"wikipedia:physicist\">physicist</a>, <a href=\"https://www.explainxkcd.com/wiki/index.php/Cueball\" title=\"Cueball\">Cueball</a>, who is staring at some (in the comic unreadable) equations and diagrams on a <a href=\"https://en.wikipedia.org/wiki/chalkboard\" class=\"extiw\" title=\"wikipedia:chalkboard\">chalkboard</a>. White Hat is neither a physicist nor a <a href=\"https://en.wikipedia.org/wiki/mathematician\" class=\"extiw\" title=\"wikipedia:mathematician\">mathematician</a>, and seems to glorify those professions. He wishes he understood Cueball's work and \"the beauty on display here.\" People who profess a love for mathematics often cite the beauty they see in pure math, how things work out so perfectly, as the reason they love math. <p>The joke is that Cueball as a physicist is doing something instead quite simple and relatable: Avoiding hard work. Solving many kinds of constraints for two unknowns isn't necessarily difficult, but can be depending on the details. Cueball clearly thinks a solution is possible but would rather find an easier route. The same could be said about the field of mathematics in general: A proof is beautiful to a mathematician when it provides <a href=\"https://en.wikipedia.org/wiki/aesthetic\" class=\"extiw\" title=\"wikipedia:aesthetic\">aesthetic</a> pleasure, usually associated with being easy to understand. A proof is elegant when it is both easy to understand and correct, and mathematical solutions are profound when useful. Record numbers of mathematics interest groups and their forums in which such work is done exist today, from academic journals predating the use of electricity to a plethora of internet math and science fora such as <a href=\"https://en.wikipedia.org/wiki/Wikipedia:Reference_desk/Mathematics\" class=\"extiw\" title=\"wikipedia:Wikipedia:Reference desk/Mathematics\">Wikipedia Reference Desks</a> and Reddit's <a rel=\"nofollow\" class=\"external text\" href=\"https://reddit.com/r/theydidthemath\">/r/theydidthemath</a> forum, which fueled a <a rel=\"nofollow\" class=\"external text\" href=\"https://i.imgur.com/l1r1VEE.png\">resurgence of the phrase \"they did the math\" as a search term in 2014,</a> because it was included in the sidebar of the <a rel=\"nofollow\" class=\"external text\" href=\"https://reddit.com/r/xkcd\">/r/xkcd</a> subreddit, where it remains five years hence, between \"Linguistics\" and \"Ask Historians\" suggesting that the term was popularized by Xkcd fans after <a rel=\"nofollow\" class=\"external text\" href=\"https://books.google.com/ngrams/graph?content=they+did+the+math&amp;case_insensitive=on&amp;year_start=1980&amp;year_end=2008&amp;corpus=15&amp;smoothing=3&amp;share=&amp;direct_url=t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0#t4%3B%2Cthey%20did%20the%20math%3B%2Cc0%3B%2Cs0%3B%3Bthey%20did%20the%20math%3B%2Cc0%3B%3BThey%20did%20the%20math%3B%2Cc0\">its initial appearance c. 1988.</a> The proliferation of mathematics fora is certainly also due to the quickly increasing overall level of education and rapidly growing numbers of internet users. </p><p>A mathematical problem involving two unknowns could be a <a href=\"https://en.wikipedia.org/wiki/system_of_linear_equations\" class=\"extiw\" title=\"wikipedia:system of linear equations\">system of linear equations</a> which can often be solved on paper, a blackboard, in a spreadsheet with solver functions, or by a <a href=\"https://en.wikipedia.org/wiki/computer_algebra_system\" class=\"extiw\" title=\"wikipedia:computer algebra system\">computer algebra system</a> such as <a rel=\"nofollow\" class=\"external text\" href=\"https://www.wolframalpha.com/input/?i=x%2By%3D10%2C+x-y%3D4&amp;lk=3\">WolframAlpha.com.</a> Linear equations are a typical kind of more general constraint satisfaction problems, which in turn are <a href=\"https://en.wikipedia.org/wiki/mathematical_optimization\" class=\"extiw\" title=\"wikipedia:mathematical optimization\">mathematical optimization</a> problems, where the minimization of a difference from a goal state (such as that all of the constraining equations are true, for example) indicates the extent to which constraints are met. Sometimes such problem solving activity arises naturally from economic transactions according to, for example, the laws of <a href=\"https://en.wikipedia.org/wiki/supply_and_demand\" class=\"extiw\" title=\"wikipedia:supply and demand\">supply and demand</a>, arising in the general context of civilization and ecology (both of which have properties associated with beauty and mathematical elegance.) Problems solved by economics are examples of <a href=\"https://en.wikipedia.org/wiki/distributed_constraint_optimization\" class=\"extiw\" title=\"wikipedia:distributed constraint optimization\">distributed constraint optimization</a> processes. When economic laws are not sufficiently satisfying constraints, that is a <a href=\"https://en.wikipedia.org/wiki/market_failure\" class=\"extiw\" title=\"wikipedia:market failure\">market failure</a>, which indicates that more artificial and manual mathematical work is required, instead of the naturally arising or otherwise automatic methods contemplated by Cueball. Other distributed constraint optimization systems can be <a href=\"https://en.wikipedia.org/wiki/crowdsourcing\" class=\"extiw\" title=\"wikipedia:crowdsourcing\">crowdsourcing</a> games, such as <a href=\"https://en.wikipedia.org/wiki/FoldIt\" class=\"extiw\" title=\"wikipedia:FoldIt\">FoldIt</a> and <a href=\"https://en.wikipedia.org/wiki/Galaxy_Zoo\" class=\"extiw\" title=\"wikipedia:Galaxy Zoo\">Galaxy Zoo</a>. </p><p>Of the graphic elements on the blackboard, the most distinctive appears to be a pair of wedges from a pie chart, where the radius of the slices is being used to represent another variable than the angles which all pie charts use to represent a primary variable. Since the cartoon is in black and white, the use of color to represent category labels or more variables may be ruled out. Such black-and-white wedges represent two variables, the meaning of which may be unknown to us, let alone their values. The only distributed constraint optimization game which uses such wedges may be the <a href=\"https://en.wikipedia.org/wiki/climate_stabilization_wedge\" class=\"extiw\" title=\"wikipedia:climate stabilization wedge\">climate stabilization wedge</a> game <a rel=\"nofollow\" class=\"external text\" href=\"https://cmi.princeton.edu/wedges/game\">from Princeton University.</a> In that wedge game, angles represent a potential number of gigatons of atmospheric carbon mitigation (out of about 38 for the circle) and radius indicates uptake, or the extent to which the mitigation solution is effective. </p><p>That game is an example of a bivariate optimization problem which might not have to be manually solved by anyone, for example under specific assumptions about the market in <a rel=\"nofollow\" class=\"external text\" href=\"https://x.company/projects/foghorn\">Project Foghorn</a> <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/WlkWabq/ioc-part-1-prototype-article-in-press.pdf\">plants</a> and <a rel=\"nofollow\" class=\"external text\" href=\"https://www.docdroid.net/SRxC3bd/power-to-gas-efficiency.pdf\">power-to-gas upgrades for natural gas</a> power plants. If such market-based approaches to distributed constraint satisfaction are successful, then the work in finding the solution would be performed not entirely by physicists, chemical engineers, mathematicians, or intentional crowdworkers playing a game to achieve the optimal solution(s), but instead in even larger part by far more widely distributed crowdworkers who are simply making their own, ideally self-interested choices regarding their demand for <a href=\"https://en.wikipedia.org/wiki/desalination\" class=\"extiw\" title=\"wikipedia:desalination\">desalinated</a> and <a href=\"https://en.wikipedia.org/wiki/drinking_water\" class=\"extiw\" title=\"wikipedia:drinking water\">potable water</a>, <a href=\"https://en.wikipedia.org/wiki/carbon-neutral_fuel\" class=\"extiw\" title=\"wikipedia:carbon-neutral fuel\">carbon-neutral liquid transportation fuel</a> and carbon-negative <a href=\"https://en.wikipedia.org/wiki/carbon_sequestration\" class=\"extiw\" title=\"wikipedia:carbon sequestration\">sequestration</a> in <a href=\"https://en.wikipedia.org/wiki/fiber-reinforced_composite\" class=\"extiw\" title=\"wikipedia:fiber-reinforced composite\">fiber-reinforced composite</a> lumber, both made from <a href=\"https://en.wikipedia.org/wiki/ocean_acidification\" class=\"extiw\" title=\"wikipedia:ocean acidification\">carbonate dissolved in seawater</a>, and for recycling the carbon in power plant flue exhaust for the <a href=\"https://en.wikipedia.org/wiki/Energy_storage\" class=\"extiw\" title=\"wikipedia:Energy storage\">storage of renewable energy</a> such as off-peak <a href=\"https://en.wikipedia.org/wiki/wind_power\" class=\"extiw\" title=\"wikipedia:wind power\">wind power</a>. The relative beauty, elegance, and simplicity of the possible solutions to such problems are subjective, and might involve strong differences of opinion between outside observers, mathematicians and engineers involved with the details, and <a href=\"https://en.wikipedia.org/wiki/Villain#Sympathetic_villain\" class=\"extiw\" title=\"wikipedia:Villain\">fossil fuel barons</a>, respected and enriched by society for their part in meeting energy demand. (See \"All Chemistry Equations\" in <a href=\"https://www.explainxkcd.com/wiki/index.php/2034:_Equations\" title=\"2034: Equations\">2034: Equations</a>.) Although the original market-focused primary use of <a href=\"https://en.wikipedia.org/wiki/ticker_tape\" class=\"extiw\" title=\"wikipedia:ticker tape\">ticker tape</a> may be a lost art, the economy is still driven by individual free will leveraging self-interested behavior to achieve social gains for civilization. </p><p>The title text continues Cueball's thought process, with the possibility of using an automatic equation solver to find the unknowns. Equation solvers are not often considered beautiful ways to address purely mathematical problems, even if they are often the most efficient and in that sense elegant solutions to applied problems in engineering. Using a formal solver with symbolic, numeric, or both methods requires making sure that the constraints (e.g. equations) are entered correctly, with parentheses balanced in their correct locations for the solution to succeed. While the <a href=\"https://en.wikipedia.org/wiki/mathematical_beauty\" class=\"extiw\" title=\"wikipedia:mathematical beauty\">beauty of mathematics</a> and pure physics may not be associated with automatic solvers in spreadsheets, general optimization methods are considered elegant in applied physics and engineering, with <a rel=\"nofollow\" class=\"external text\" href=\"http://entsphere.com/pub/pdf/1957%20Jaynes,%20ShannonMaxEntBoltzmann.pdf\">Jaynes (1957)</a> cited more than 12,000 times on Google Scholar, including by <a rel=\"nofollow\" class=\"external text\" href=\"https://www.researchgate.net/publication/234147180_Maximum_Entropy_Image_Restoration_in_Astronomy\">a paper cited</a> by the <a rel=\"nofollow\" class=\"external text\" href=\"https://arxiv.org/abs/1711.01286\">first black hole image astronomers</a> for example. </p> "
}