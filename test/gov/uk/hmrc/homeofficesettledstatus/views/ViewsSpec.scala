package gov.uk.hmrc.homeofficesettledstatus.views

import javax.inject.Inject
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.i18n.{Lang, Messages, MessagesImpl}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import gov.uk.hmrc.homeofficesettledstatus.controllers.HomeOfficeSettledStatusFrontendController
import gov.uk.hmrc.homeofficesettledstatus.controllers.HomeOfficeSettledStatusFrontendController.HomeOfficeSettledStatusFrontendForm
import gov.uk.hmrc.homeofficesettledstatus.models.HomeOfficeSettledStatusFrontendModel
import gov.uk.hmrc.homeofficesettledstatus.views.html._
import uk.gov.hmrc.play.config.{AssetsConfig, GTMConfig, OptimizelyConfig}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF, Input, ReportAProblemLink}
import uk.gov.hmrc.play.views.html.layouts._
import views.html.layouts.GovUkTemplate

class ViewsSpec @Inject()(govUkWrapper: govuk_wrapper, mainTemplate: main_template)
    extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar {

  implicit val lang: Lang = Lang("eng")

  private val filledForm = HomeOfficeSettledStatusFrontendController.HomeOfficeSettledStatusFrontendForm.fill(
    HomeOfficeSettledStatusFrontendModel(
      name = "My contact name",
      postcode = Some("AA1 1AA"),
      telephoneNumber = Some("9876543210"),
      emailAddress = Some("my@email.com")))

  "error_template view" should {
    "render title, heading and message" in new App {
      val pageTitle = "My custom page title"
      val heading = "My custom heading"
      val message = "My custom message"
      val html = new error_template(govUkWrapper).render(
        pageTitle = pageTitle,
        heading = heading,
        message = message,
        messages = MessagesImpl(lang, stubMessagesApi()),
        configuration = app.configuration)
      val content = contentAsString(html)
      content should include(pageTitle)
      content should include(heading)
      content should include(message)

      val html2 =
        new error_template(govUkWrapper)
          .f(pageTitle, heading, message)(MessagesImpl(lang, stubMessagesApi()), app.configuration)
      contentAsString(html2) shouldBe (content)
    }
  }

  "start view" should {
    "render title and messages" in new App {
      val input = new Input()
      val form = new FormWithCSRF()
      val errorSummary = new ErrorSummary()
      val html = new start_page(mainTemplate, input, form, errorSummary)
        .render(
          homeOfficeSettledStatusFrontendForm = HomeOfficeSettledStatusFrontendForm,
          request = FakeRequest(),
          messages = MessagesImpl(lang, stubMessagesApi()),
          config = app.configuration
        )
      val content = contentAsString(html)

      implicit val messagesProvider = MessagesImpl(lang, stubMessagesApi())
      content should include(Messages("start.title"))
      content should include(Messages("start.label"))
      content should include(Messages("start.intro"))
      content should include(Messages("start.helpdesklink.text1"))
      content should include(Messages("start.helpdesklink.text2"))

      val html2 = new start_page(mainTemplate, input, form, errorSummary)
        .f(HomeOfficeSettledStatusFrontendForm)(FakeRequest(), MessagesImpl(lang, stubMessagesApi()), app.configuration)
      contentAsString(html2) shouldBe (content)
    }
  }

  "main_template view" should {
    "render all supplied arguments" in new App {
      val sidebar = new Sidebar()
      val article = new Article()
      val view = new main_template(sidebar, article, govUkWrapper)
      val html = view.render(
        title = "My custom page title",
        sidebarLinks = Some(Html("My custom sidebar links")),
        contentHeader = Some(Html("My custom content header")),
        bodyClasses = Some("my-custom-body-class"),
        mainClass = Some("my-custom-main-class"),
        scriptElem = Some(Html("My custom script")),
        mainContent = Html("My custom main content HTML"),
        messages = MessagesImpl(lang, stubMessagesApi()),
        request = FakeRequest(),
        configuration = app.configuration
      )

      val content = contentAsString(html)
      content should include("My custom page title")
      content should include("My custom sidebar links")
      content should include("My custom content header")
      content should include("my-custom-body-class")
      content should include("my-custom-main-class")
      content should include("My custom script")
      content should include("My custom main content HTML")

      val html2 = view.f(
        "My custom page title",
        Some(Html("My custom sidebar links")),
        Some(Html("My custom content header")),
        Some("my-custom-body-class"),
        Some("my-custom-main-class"),
        Some(Html("My custom script"))
      )(Html("My custom main content HTML"))(MessagesImpl(lang, stubMessagesApi()), FakeRequest(), app.configuration)
      contentAsString(html2) shouldBe (content)
    }
  }

  "govuk wrapper view" should {
    "render all of the supplied arguments" in new App {
      val config = mock[Configuration]
      val optimizelyConfig = new OptimizelyConfig(config)
      val optimizelySnippet = new OptimizelySnippet(optimizelyConfig)
      val assetsConfig = new AssetsConfig(config)
      val gtmConfig = new GTMConfig(config)
      val gtmSnippet = new GTMSnippet(gtmConfig)
      val head = new Head(optimizelySnippet, assetsConfig, gtmSnippet)
      val headerNav = new HeaderNav()
      val footer = new Footer(assetsConfig)
      val footerLinks = new FooterLinks()
      val serviceInfo = new ServiceInfo()
      val mainContentHeader = new MainContentHeader()
      val mainContent = new MainContent()
      val reportAProblemLink = new ReportAProblemLink()
      val govUkTemplate = new GovUkTemplate()

      val html = new govuk_wrapper(
        head,
        headerNav,
        footer,
        footerLinks,
        serviceInfo,
        mainContentHeader,
        mainContent,
        reportAProblemLink,
        govUkTemplate).render(
        title = "My custom page title",
        mainClass = Some("my-custom-main-class"),
        mainDataAttributes = Some(Html("myCustom=\"attributes\"")),
        bodyClasses = Some("my-custom-body-class"),
        sidebar = Html("My custom sidebar"),
        contentHeader = Some(Html("My custom content header")),
        mainContent = Html("My custom main content"),
        serviceInfoContent = Html("My custom service info content"),
        scriptElem = Some(Html("My custom script")),
        gaCode = Seq("My custom GA code"),
        messages = MessagesImpl(lang, stubMessagesApi()),
        configuration = app.configuration
      )

      val content = contentAsString(html)
      content should include("My custom page title")
      content should include("my-custom-main-class")
      content should include("myCustom=\"attributes\"")
      content should include("my-custom-body-class")
      content should include("My custom sidebar")
      content should include("My custom content header")
      content should include("My custom main content")
      content should include("My custom service info content")
      content should include("My custom script")

      val html2 = new govuk_wrapper(
        head,
        headerNav,
        footer,
        footerLinks,
        serviceInfo,
        mainContentHeader,
        mainContent,
        reportAProblemLink,
        govUkTemplate).f(
        "My custom page title",
        Some("my-custom-main-class"),
        Some(Html("myCustom=\"attributes\"")),
        Some("my-custom-body-class"),
        Html("My custom sidebar"),
        Some(Html("My custom content header")),
        Html("My custom main content"),
        Html("My custom service info content"),
        Some(Html("My custom script")),
        Seq("My custom GA code")
      )(MessagesImpl(lang, stubMessagesApi()), app.configuration)
      contentAsString(html2) shouldBe (content)
    }
  }
}
