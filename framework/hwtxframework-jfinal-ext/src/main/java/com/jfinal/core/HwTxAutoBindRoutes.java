package com.jfinal.core;

import com.jfinal.config.Routes;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Logger;

public class HwTxAutoBindRoutes extends Routes {

    protected final Logger logger = Logger.getLogger(getClass());

    /**
     * 返回controller映射的路径
     * @param controller
     * @return
     */
    public String config(Controller controller) {
        Class<? extends Controller> controllerClass = controller.getClass();
        ControllerBind controllerBind = controllerClass.getAnnotation(ControllerBind.class);
        if (controllerBind == null) {
            String kk = HwTx.controllerKey(controllerClass, suffix);
            this.add(kk, controller);
            if (logger.isDebugEnabled()) {
                logger.debug("routes.add(" + kk + ", " + controllerClass.getName() + ")");
            }
            return kk;
        } else {
            String cKey = controllerBind.controllerKey();
            if (PathKit.isVariable(cKey)) {
                cKey = PathKit.getVariableValue(cKey);
            }
            String kk = cKey;
            if (StrKit.isBlank(controllerBind.viewPath())) {
                this.add(kk, controller);
                logger.debug("routes.add(" + kk + ", " + controllerClass.getName() + ")");
            } else {
                this.add(kk, controller, controllerBind.viewPath());
                logger.debug("routes.add(" + kk + ", " + controller + "," + controllerBind.viewPath() + ")");
            }
            return kk;
        }
    }

    private String suffix = "Controller";

    public HwTxAutoBindRoutes suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public void config() {

    }
}
