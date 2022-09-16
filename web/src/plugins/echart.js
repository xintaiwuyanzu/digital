import vCharts from '@dr/auto/lib/plugins/vCharts'
import AsyncValidator from "async-validator";

// 处理validate的中文显示
const oldMessage = AsyncValidator.prototype.messages
AsyncValidator.prototype.messages = function (message) {
    const returnMessage = oldMessage.apply(this, message)
    if (returnMessage.required) {
        returnMessage.required = '%s 不能为空！'
    }
    return returnMessage
}
export default vCharts
