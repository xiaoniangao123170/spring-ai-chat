package com.ming.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 天气工具：方法上加 @Tool 后即可被 AI 调用
 *
 * @author Ming
 */
public class WeatherTool {

    @Tool(description = "获取今天的天气")
    public String getWeather(@ToolParam(description = "城市名称") String cityName) {
        if ("上海".equals(cityName)) {
            return "今天上海的天气是：晴转多云，再转大雨，再转冰雹，1103摄氏度";
        } else if ("北京".equals(cityName)) {
            return "今天北京的天气是：多云转晴，再转小雨，再转闪电，1104摄氏度。";
        } else {
            return "不知道";
        }
    }
}
