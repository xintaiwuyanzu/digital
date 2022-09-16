import {Sortable} from 'sortablejs'
import abstractDragImageContainer from "./abstractDragImageContainer";

function createCollapseItem(data, context, index) {
    const id = data.id
    const childData = context.getImageDataByPid(data.id)
    let empty = ''
    if (childData.length === 0) {
        const imgSlot = () => '暂无数据！！'
        empty = <el-empty class="tree-empty" description="请选择并拖拽右侧图片到此处"
                          scopedSlots={{image: imgSlot}}/>
    }
    const child = childData.map(d => {
        let tag = ''
        if (d.tag) {
            tag = <el-tag class='tag' type='primary'>已标记</el-tag>
        }
        return <span data-id={d.id} class="image-item"
                     data-type='left-item'
                     key={d.id}
                     onClick={() => context.scrollTo(d.id)}>{d.label}{tag}</span>
    })
    context.$nextTick(() => {
        if (!context.$dirSort) {
            context.$dirSort = {}
        }
        if (!context.$dirSort[id] && context.$refs[id]) {
            context.$dirSort[data.id] = Sortable.create(context.$refs[data.id], {
                group: {name: 'items', pull: 'items', put: ['imgs', 'items']},
                multiDrag: true,
                filter: '.el-empty',
                multiDragKey: "ctrl",
                onEnd: context.treeItemDragEnd,
                sort: true
            })
        }
    })
    const className = id === context.currentDirId ? 'currentSelect' : ''
    const headerSlot = () => <span>{data.label}({childData.length})</span>
    return (
        <el-collapse-item name={id} data-id={id} data-index={index}
                          data-type='left-dir'
                          class={className} {...{scopedSlots: {title: headerSlot}}}>
            <section data-id={id} data-index={index} ref={id} class='left-c' data-type='itemContainer'>
                {empty}
                {child}
            </section>
        </el-collapse-item>
    )
}

/**
 * 一个大容器包含所有的图片处理功能
 */
export default {
    extends: abstractDragImageContainer,
    methods: {
        //右侧图片拖拽
        imageDragEnd(e) {
            let {item, items, newIndex, to, clone, clones, from} = e
            if (from === to) {
                return
            }
            if (items.length === 0) {
                //只操作一个的时候
                items.push(item)
                clones.push(clone)
            }
            const allImgs = this.getAllImgData()
            const itemIds = items.map(i => i.dataset.id)
            this.recordHistory(allImgs)
            if (items.length > 0) {
                const targetType = to.dataset.type
                if (targetType === 'leftTree') {
                    //从图片拖拽到左侧指定的文件夹
                    //文件夹
                    const dir = this.splitParent.volumesData[newIndex]
                    const dirImgs = this.getImageDataByPid(dir.id)
                    const allImgIds = new Set()
                    dirImgs.forEach(d => allImgIds.add(d.id))
                    items.forEach(i => allImgIds.add(i.dataset.id))
                    let index = 0;
                    allImgs.forEach(i => {
                        if (allImgIds.has(i.id)) {
                            i.pid = dir.id
                            i.dirIndex = index++
                        }
                        if (itemIds.indexOf(i.id) >= 0) {
                            i.tag = false
                        }
                    })
                } else if (targetType === 'itemContainer') {
                    //左侧文件夹内某个文件
                    const dir = this.splitParent.volumesData.find(v => v.id === to.dataset.id)
                    const dirImgs = this.getImageDataByPid(dir.id)
                    if (dirImgs.length === 0) {
                        newIndex = 0
                    }
                    const copyIds = items.map(i => i.dataset.id)
                    if (newIndex === dirImgs.length) {
                        //直接追加到最后
                        for (let i = dirImgs.length - 1; i > -1; i--) {
                            const id = dirImgs[i].id
                            //需要排除掉重复拖拽的图片
                            if (copyIds.indexOf(id) < 0) {
                                copyIds.unshift(id)
                            }
                        }
                    } else if (newIndex === 0) {
                        //追加到最前
                        for (let i = 0; i < dirImgs.length; i++) {
                            const id = dirImgs[i].id
                            //需要排除掉重复拖拽的图片
                            if (copyIds.indexOf(id) < 0) {
                                copyIds.push(id)
                            }
                        }
                    } else {
                        //追加在中间
                        for (let i = newIndex - 1; i > -1; i--) {
                            const id = dirImgs[i].id
                            //需要排除掉重复拖拽的图片
                            if (copyIds.indexOf(id) < 0) {
                                copyIds.unshift(id)
                            }
                        }
                        for (let i = newIndex; i < dirImgs.length; i++) {
                            const id = dirImgs[i].id
                            //需要排除掉重复拖拽的图片
                            if (copyIds.indexOf(id) < 0) {
                                copyIds.push(id)
                            }
                        }
                    }
                    allImgs.forEach(i => {
                        const copyIndex = copyIds.indexOf(i.id)
                        if (copyIndex >= 0) {
                            i.pid = dir.id
                            i.dirIndex = copyIndex + 1
                        }
                        if (itemIds.indexOf(i.id) >= 0) {
                            i.tag = false
                        }
                    })
                }
                //删除掉所有clone的元素
                for (let i = 0; i < clones.length; i++) {
                    const source = items[i]
                    const cloned = clones[i]
                    //因为是clone模式，所以删除掉clone的节点数据
                    from.insertBefore(source, cloned)
                    from.removeChild(cloned)
                }
                this.$forceUpdate()
            }
        },
        //左侧文件夹内文件名称拖拽
        treeItemDragEnd(e) {
            let {item, items, to, from} = e
            if (items.length === 0) {
                //只操作一个的时候
                items.push(item)
            }
            const dirId = to.dataset.id
            const dirImgs = this.getImageDataByPid(dirId)
            const allIds = Array.from(to.children).map(i => i.dataset.id)
            this.recordHistory(this.getAllImgData())
            if (to === from) {
                dirImgs.forEach(i => i.dirIndex = allIds.indexOf(i.id) + 1)
            } else {
                this.getImageDataByPid(from.dataset.id).forEach(i => {
                    const index = allIds.indexOf(i.id)
                    if (index >= 0) {
                        i.dirIndex = index + 1
                        i.pid = dirId
                    }
                })
                dirImgs.forEach(i => i.dirIndex = allIds.indexOf(i.id) + 1)
            }
            this.$forceUpdate()
        }
    },
    watch: {
        //图片数据重新加载
        'splitParent.ImgData'(n) {
            if (!this.$imgSort && n && n.length > 0) {
                this.$nextTick(() => {
                    this.$imgSort = Sortable.create(this.$refs.scrollContainer.$el, {
                        group: {name: "imgs", put: false, pull: 'clone'},
                        multiDrag: true,
                        multiDragKey: "ctrl",
                        onSelect: this.select,
                        onDeselect: this.select,
                        sort: false,
                        onEnd: this.imageDragEnd
                    })
                    //记录最原始的操作历史
                    this.recordHistory(n, true)
                })
            }
        },
        //文件夹数据重新加载
        'splitParent.volumesData'(v) {
            if (v && v.length > 0) {
                this.isAll = true
                this.$nextTick(() => {
                    this.recordDir(v)
                    const ids = v.map(i => i.id)
                    if (!this.$treeSort && this.$refs.leftTree) {
                        this.$treeSort = Sortable.create(this.$refs.leftTree.$el, {
                            group: {name: 'tree', put: 'imgs', pull: false},
                            sort: false
                        })
                    }
                    if (this.$dirSort) {
                        //删除掉之前创建的
                        Object.keys(this.$dirSort).forEach(d => {
                            if (ids.indexOf(d) < 0) {
                                this.$dirSort[d].destroy()
                                delete this.$dirSort[d]
                            }
                        })
                    }
                })
            }
        }
    },
    beforeDestroy() {
        if (this.$imgSort) {
            this.$imgSort.destroy()
            this.$imgSort = null
        }
        if (this.$dirSort) {
            Object.keys(this.$dirSort).forEach(d => {
                this.$dirSort[d].destroy()
            })
            this.$dirSort = null
        }
        if (this.$treeSort) {
            this.$treeSort.destroy()
            this.$treeSort = null
        }
    },
    render() {
        const imgData = this.getShowData()
        let tagButton = ''
        if (!this.isAll) {
            tagButton = <el-button style="float: right;padding:4px 8px;margin-right:10px;" type='primary'
                                   disabled={!this.currentImg.id}
                                   onClick={this.tag}>{this.currentImg.tag ? '取消标记结尾' : '标记结尾'}
            </el-button>
        }
        return (
            <el-row style="height: 100%;">
                {this.createImageViewer(imgData)}
                <Split gutterSize={6} style="height: 100%;">
                    <SplitArea size={20} class='left-container'>
                        <el-tag>文件夹结构</el-tag>
                        <el-collapse class="drag-tree" ref='leftTree'
                                     data-type='leftTree'
                                     v-model={this.currentDirIds}
                                     onChange={this.dirChange}>
                            {this.splitParent.volumesData.map((v, i) => createCollapseItem(v, this, i))}
                        </el-collapse>
                    </SplitArea>
                    <SplitArea size={80} class="right-container">
                        <section style="padding: 10px;border-bottom:1px solid gray">
                            <el-tag style="margin-right: 10px;font-size: 14px;font-weight: bold" type="danger">
                                已选择：{this.selectCount}张
                            </el-tag>
                            <radio-async mapper={this.radioMap} v-model={this.radioValue} style="margin:0px 10px"/>
                            <el-switch active-text="所有图片" inactive-text="显示当前文件夹" v-model={this.isAll}
                                       style="float: right"/>
                            <el-button style="float: right;padding:4px 8px;margin-right:10px;" type='primary'
                                       onClick={this.reset}>复原
                            </el-button>
                            <el-button style="float: right;padding:4px 8px;margin-right:10px;" type='primary'
                                       onClick={this.resetPre}>上一步
                            </el-button>
                            <el-checkbox v-model={this.autoScroll} style="margin-right: 10px;float: right">自动导航
                            </el-checkbox>
                            {tagButton}
                        </section>
                        {this.createImageChildren(imgData)}
                    </SplitArea>
                </Split>
            </el-row>
        )
    }
}