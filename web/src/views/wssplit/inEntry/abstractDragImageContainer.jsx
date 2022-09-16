import {MultiDrag, Sortable} from 'sortablejs'
import ImageViewer from "@/views/wssplit/inEntry/ImagePreviewer";

if (!Sortable.$mountDrag) {
    Sortable.mount(new MultiDrag())
    Sortable.$mountDrag = true
}
//操作历史数组
let historyArr = []
let prevOverflow = '';
let lastClick = 0
let lastIndex = 0

/**
 * 拖拽容器抽象父类，
 * 处理逻辑太多了，抽离出来一些基础逻辑。
 *
 * 父类处理数据相关的内容
 * 子类处理拖拽相关的逻辑
 */
export default {
    inject: ['splitParent'],
    data() {
        return {
            currentDirId: '',
            currentDirIds: [],
            radioValue: 4,
            radioMap: {
                2: '二列',
                4: '四列',
                6: '六列'
            },
            selectCount: 0,
            isAll: true,
            preview: false,
            viewIndex: 0,
            autoScroll: true,
            hightId: '',
            //文件夹排序，key是文件夹Id，value是排序
            dirOrders: {},
            //当前选中的图片
            currentImg: {}
        }
    },
    methods: {
        //图片排序方法
        sortImg(a, b) {
            const aDirOrder = this.dirOrders[a.pid] >= 0 ? this.dirOrders[a.pid] : -1
            const bDirOrder = this.dirOrders[b.pid] >= 0 ? this.dirOrders[b.pid] : -1
            if (aDirOrder === bDirOrder) {
                if (aDirOrder === -1) {
                    return a.sourceIndex - b.sourceIndex
                } else {
                    return a.dirIndex - b.dirIndex
                }
            } else {
                return aDirOrder - bDirOrder
            }
        },
        //滚动到指定id的组件
        scrollTo(id) {
            const comp = this.$refs[id]
            if (comp) {
                if (comp.$el) {
                    comp.$el.scrollIntoView({behavior: 'smooth'})
                } else if (comp.scrollIntoView) {
                    comp.scrollIntoView({behavior: 'smooth'})
                }
            }
        },
        showPreview(index, v) {
            this.currentImg = v
            const now = new Date().getTime()
            if (now - lastClick < 300 && lastIndex === index) {
                prevOverflow = document.body.style.overflow;
                document.body.style.overflow = 'hidden';
                this.viewIndex = index
                this.preview = true
            }
            lastClick = now
            lastIndex = index
        },
        closeView() {
            document.body.style.overflow = prevOverflow;
            this.hightId = ''
            this.preview = false;
        },
        onSwitch(v) {
            this.viewIndex = v
            const item = this.getShowData()[v]
            this.hightId = item.id
            this.scrollTo(this.hightId)
        },
        getImageDataByPid(pid) {
            return this.splitParent.ImgData.filter(v => v.pid === pid).sort(this.sortImg)
        },
        //获取当前显示的所有图片数组
        getShowData() {
            if (this.isAll) {
                return this.getAllImgData()
            } else {
                return this.getImageDataByPid(this.currentDirId)
            }
        },
        //获取所有排序之后的图片
        getAllImgData() {
            return [...this.splitParent.ImgData].sort(this.sortImg)
        },
        //重置
        reset() {
            const first = historyArr[0]
            historyArr.length = 1
            this.splitParent.ImgData.forEach(i => {
                const source = first[i.id]
                i.dirIndex = source.dirIndex
                i.pid = source.pid
                i.tag = source.tag
            })
            this.$forceUpdate()
        },
        //退回上一步
        resetPre() {
            let target = {}
            if (historyArr.length === 1) {
                target = historyArr[0]
            } else {
                target = historyArr[historyArr.length - 1]
                historyArr.length = historyArr.length - 1
            }
            this.splitParent.ImgData.forEach(i => {
                const source = target[i.id]
                i.dirIndex = source.dirIndex
                i.pid = source.pid
                i.tag = source.tag
            })
            this.$forceUpdate()
        },
        tag() {
            this.recordHistory(this.getAllImgData())
            this.currentImg.tag = !this.currentImg.tag
            this.$forceUpdate()
        },
        select(v) {
            this.selectCount = v.items.length
        },
        dirChange(v) {
            if (v.length > 0) {
                this.currentDirId = v[v.length - 1]
            } else {
                this.currentDirId = ''
            }
        },
        createImageViewer(imgData) {
            if (this.preview) {
                const item = imgData[this.viewIndex]
                let label = item.label
                if (item.pid) {
                    label += '/' + this.splitParent.volumesData.find(v => v.id === item.pid).label
                }
                const props = {
                    initialIndex: this.viewIndex,
                    onSwitch: this.onSwitch,
                    onClose: this.closeView,
                    tip: `${label}(${this.viewIndex + 1}/${imgData.length})`,
                    urlList: imgData.map(v => v.filePath),
                }
                return <ImageViewer {...{props}} />
            }
        },
        //记录操作历史
        recordHistory(imgData, isFirst) {
            const record = {}
            imgData.forEach(({id, pid, dirIndex, tag = false}) => record[id] = {dirIndex, pid, tag})
            if (isFirst) {
                historyArr = [record]
            } else {
                historyArr.push(record)
            }
        },
        /**
         * 记录文件夹的排序等相关信息，不用重复计算了
         */
        recordDir(v) {
            this.dirOrders = {}
            v.forEach((d, i) => this.dirOrders[d.id] = i)
        },
        createImageItem(v, index) {
            let classValue = 'split-img-item '
            if (v.id === this.hightId) {
                classValue += ' preview'
            }
            let label = v.label
            if (v.pid) {
                const dir = this.splitParent.volumesData.find(i => i.id === v.pid)
                label += '/' + dir.label
            }
            let tag = ''
            if (v.tag) {
                tag = <el-tag type='primary'>已标记</el-tag>
            }
            return (
                <el-col class={classValue} span={24 / this.radioValue} key={v.id} ref={v.id}
                        data-id={v.id} data-type='right-item'
                        nativeOnClick={() => this.showPreview(index, v)}>
                    <el-image src={v.filePath} lazy={true} scroll-container={this.$refs.scrollContainer}
                              fit="scale-down">
                        <div slot="placeholder">
                            <div><i class="el-icon-loading"/></div>
                            加载中<span class="dot">...</span>
                        </div>
                    </el-image>
                    <span style="text-align: center;display:block;height: 20px">
                      {label}{tag}
                    </span>
                </el-col>
            );
        },
        createImageChildren(imgData) {
            if (imgData && imgData.length > 0) {
                return (<el-row ref='scrollContainer' class="img-container">
                    {imgData.map((v, i) => this.createImageItem(v, i))}
                </el-row>)
            } else {
                return <el-empty image-size={200} description="暂无数据，请选择左侧文件夹或者切换显示所有图片"/>
            }
        },
    },
    watch: {
        currentDirId(v) {
            if (this.autoScroll) {
                const datas = this.getImageDataByPid(v)
                if (datas.length > 0) {
                    this.scrollTo(datas[0].id)
                }
            }
        }
    }
}