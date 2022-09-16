const client = require('socket.io-client')
let autoListeners = []

export default function (port = 12518) {
    //TODO 这里应该添加唤起本地协议的东西
    let socket = null
    const checkConnect = async () => {
        if (socket && socket.connected) {
            return socket
        } else {
            return new Promise((resolve, reject) => {
                if (socket) {
                    if (socket.connected) {
                        resolve(socket)
                    } else {
                        resolve(new Promise(resolve1 => {
                            socket.connect()
                            socket.once('connect', () => resolve1(socket));
                            socket.on('scanner', data => {
                                autoListeners.forEach(v => v(data))
                            })
                            //启动读卡器的监听
                            socket.on('openCardAutoScan', data => {
                                autoListeners.forEach(v => v(data))
                            })
                        }))
                    }
                } else {
                    resolve(new Promise(resolve1 => {
                        socket = client('http://127.0.0.1:' + port)
                        socket.once('connect', () => {
                            resolve1(socket)
                        });
                        socket.on('scanner', data => {
                            autoListeners.forEach(v => data)
                        })
                    }))
                }
            })
        }
    }
    /**
     * 工具类，真正调用的方法
     * @param mtd 方法名称
     * @param param 参数
     * @returns {function(): Promise<function(): Promise>}
     */
    const doEmit = async (mtd, param) => {
        const socket = await checkConnect()
        return new Promise(r => {
            socket.emit(mtd, param, d => {
                r(d)
            })
        })
    }
    return {
        /**
         * 读TID区
         */
        getScanner: async () => await doEmit('getScanner'),
        /**
         * 写EPC区
         */
        openUi: async (data) => await doEmit(
            'openUi',
            {
                name: data,
                name1: data
            }),
        /**
         * 读EPC区
         * @param data
         * @returns {Promise<*>}
         */
        scanners: async () => await doEmit(
            'scanner',
            {
                antNo: 0
            }),
        /**
         * 获取设备信息
         */
        getDeviceInfo: async () => await doEmit('getDeviceInfo'),
        /**
         * 获取功率
         */
        getPower: async () => await doEmit('getPower'),
        /**
         * 设置功率
         */
        setPower: async () => await doEmit('setPower',
            //超高频标签需传此参数。高频则为空
            {
                //是否启用。0为开启，1为关闭
                ant01Enabled: 0,
                //功率值，大唐为10-30
                ant01Power: 18,
                //天线扫描时间。取值1-30
                ant01WorkTimer: 15
            }),
        /**
         * 打开自动扫描
         * @param cb 扫描完数据之后的监听函数
         */
        scanner: async (cb, path, name, order, upurl) => {
            return checkConnect().then(socket => {
                autoListeners = [cb]
                socket.emit("scanner", {
                    path: path,
                    name: name,
                    order: order,
                    upurl: upurl
                })
            }).catch(e => {
                console.log(e)
            })
        }
    }
}

