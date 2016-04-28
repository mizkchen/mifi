import me.kingka.service.MiFiServer;
import me.kingka.locale.I18n;
import me.kingka.network.configuration.ConfigurationFactory;
import org.apache.log4j.Logger;

/**
 * 启动程序
 *
 * @author swift.apple
 */
public class Application {
    public static void main(String[] args) {
        final I18n locale = I18n.getInstance().load("zh_CN");
        final Logger logger = Logger.getLogger(Application.class);
        if (args.length != 1) {
            logger.info(locale.get("text.001"));
            return;
        }
        final ConfigurationFactory factory = ConfigurationFactory.getInstance().setConfigPath(args[0]).load();
        if (!factory.isLoaded()) {
            logger.info(locale.get("text.003"));
            return;
        }
        MiFiServer.getInstance().load().run();

    }
}
