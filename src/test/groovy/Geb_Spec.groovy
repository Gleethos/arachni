import core.NativeConsole
import core.WebioServer
import geb.spock.GebSpec
import spock.lang.Ignore

class Geb_Spec extends GebSpec {

    def setupSpec() {
        System.setProperty("webdriver.gecko.driver","C:/Users/danie/Documents/development/web/drivers/geckodriver.exe")

    }

    def cleanupSpec() {
    }

    @Ignore
    def 'Start browser'() {
        given:
            def console = new NativeConsole()
            def server = new WebioServer(8888, new NativeConsole(), true, { console })
            def runner = new Thread(server)
        when :
            runner.start()
        and :
            go "http://localhost:8888/crud"
        and :
            Thread.sleep(1_000_000)

        then : true
    }


    @Ignore
    def 'Test me'() {
        given :
            def console = new NativeConsole()
            def server = new WebioServer(8888, new NativeConsole(), true, {console})
            def runner = new Thread(server)

        when :
            runner.start()
        and :
            go "http://localhost:8888/crud"
        and :
            Thread.sleep(1000)
        and:
            $('button', id:'database_settings_button').click()
        and :
            def fieldContents = []
            $(id:'sql_source_file_option_menu').children().each {
                it.click()
                fieldContents.add($(id:'sql_world_source').value().toString())
            }

        then :
            fieldContents.size() > 0
        and :
            fieldContents.any({it.endsWith("taleworld")})
            fieldContents.any({it.endsWith("testworld")})

        when :
            $('button', id:'tale_relations_tab_button').click()
        then :
            $('button', id:'tales_tab_button').text() == "Tales"
            $('button').collect({it.attr('style')}).every ({it == 'display:none'})
            $('button', id:'tales_tab_button').attr('style') == 'display:none'

        when :
            interact {
                moveToElement($('button', id:'tales_tab_button'))
            }
            $('button', id:'tales_tab_button').click()

        then:
            true

        cleanup:
            runner.interrupt()
            server.shutdown()

    }




}