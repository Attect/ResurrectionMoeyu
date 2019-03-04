package jp.co.a_tm.moeyu.live2d.motion;

import java.util.ArrayList;
import java.util.HashMap;
import jp.co.a_tm.moeyu.live2d.LAppLive2DManager;
import jp.live2d.ALive2DModel;
import jp.live2d.motion.AMotion;
import jp.live2d.motion.MotionQueueManager.MotionQueueEnt;
import jp.live2d.util.Json;
import jp.live2d.util.Json.Value;
import jp.live2d.util.UtFile;

public class LAppExpressionMotion extends AMotion {
    public static final String EXPRESSION_A = "A";
    public static final String EXPRESSION_B = "B";
    public static final String EXPRESSION_C = "C";
    public static final String EXPRESSION_D = "D";
    private static final String EXPRESSION_DEFAULT = "DEFAULT";
    public static final String EXPRESSION_E = "E";
    public static final String[] EXPRESSION_ARRAY = new String[]{EXPRESSION_A, EXPRESSION_B, EXPRESSION_C, EXPRESSION_D, EXPRESSION_E};
    ArrayList<String> idList = new ArrayList();
    float[] values = null;

    public static HashMap<String, LAppExpressionMotion> loadExpressionJson(LAppLive2DManager live2DMgr, String dir, String filename) {
        HashMap<String, LAppExpressionMotion> expressions = new HashMap();
        try {
            Value mo = Json.parseFromBytes(UtFile.load(live2DMgr.getFileManager().open_resource(dir + "/" + filename)));
            Value defaultExpr = mo.get(EXPRESSION_DEFAULT);
            for (Object key : mo.keySet()) {
                if (!EXPRESSION_DEFAULT.equals(key)) {
                    expressions.put((String) key, new LAppExpressionMotion(defaultExpr, mo.get((String) key)));
                }
            }
        } catch (Exception e) {
            System.err.printf("failed to loadExpressionJson :: %s\n", new Object[]{filename});
        }
        return expressions;
    }

    public LAppExpressionMotion(Value defaultExpr, Value expr) {
        String id;
        super.setFadeIn(expr.get("FADE_IN").toInt(1000));
        super.setFadeOut(expr.get("FADE_OUT").toInt(1000));
        Value defaultParams = defaultExpr.get("PARAMS");
        Value params = expr.get("PARAMS");
        for (Object id2 : params.keySet()) {
            this.idList.add((String) id2);
        }
        this.values = new float[this.idList.size()];
        for (int i = this.idList.size() - 1; i >= 0; i--) {
            id = (String) this.idList.get(i);
            float defaultV = defaultParams.get(id).toFloat(0.0f);
            this.values[i] = params.get(id).toFloat(0.0f) - defaultV;
        }
    }

    public void updateParamExe(ALive2DModel model, long timeMSec, float weight, MotionQueueEnt motionQueueEnt) {
        for (int i = this.idList.size() - 1; i >= 0; i--) {
            model.addToParamFloat((String) this.idList.get(i), this.values[i], weight);
        }
    }
}
