<template>
    <section>
        <nac-info :back="true">
        </nac-info>
        <div class="index_main card" v-loading="loading">
            <el-card shadow="always">
                <span class="title" slot="header">房屋设置
                </span>
                <el-form :model="houseForm" ref="form" label-width="110px" inline="false">
                    <el-form-item label="园区：" prop="park">
                        <el-select v-model="houseForm.park" placeholder="请选择园区" clearable filterable
                                   @change="getBuildByPark">
                            <el-option v-for="item in parkList" :key="item.id" :label="item.parkName" :value="item.id"/>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="建筑：" prop="building">
                        <el-select v-model="houseForm.building" placeholder="请选择建筑" clearable
                                   filterable @change="getFloorByBuild">
                            <el-option v-for="item in builds" :key="item.id" :label="item.buildingName" :value="item.id"/>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="楼层号：" prop="floor">
                        <el-select v-model="houseForm.floor" placeholder="请选择楼层号" clearable
                                   filterable>
                            <el-option v-for="item in floors" :key="item.id" :label="item.floorNum" :value="item.id"/>
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="searchRoom" size="mini">搜 索</el-button>
                        <el-button  @click="resetRoom" size="mini">重 置</el-button>
                    </el-form-item>
                </el-form>
            </el-card>
            <el-card shadow="hover" style="height: 55vh">
                <span class="title" slot="header">房间基本信息</span>
                <div class="table-container">
                    <el-table :data="data" border max-height="450px" @selection-change="handleTableSelect" >
                        <el-table-column type="selection" width="55" :selectable="checkSelectable">
                        </el-table-column>
                        <el-table-column prop="roomCode" label="房间号" align="center" show-overflow-tooltip>
                            <template slot-scope="scope">
                                <el-button type="text" size="small" @click="detail(scope.row)">{{scope.row.roomCode}}
                                </el-button>
                            </template>
                        </el-table-column>
                        <el-table-column prop="layout" label="布局" align="center" show-overflow-tooltip/>
                        <el-table-column prop="orientation" label="朝向" align="center" show-overflow-tooltip/>
                        <el-table-column prop="floorNum" label="所属楼层号" align="center" show-overflow-tooltip/>
                        <el-table-column label="所属建筑" width="120" align="center" header-align="center"
                                         show-overflow-tooltip>
                            <template slot-scope="scope">
                                {{scope.row.buildingID|getBuildings}}
                            </template>
                        </el-table-column>
                        <el-table-column label="所属园区" width="120" align="center" header-align="center"
                                         show-overflow-tooltip>
                            <template slot-scope="scope">
                                {{scope.row.parkId|getParks}}
                            </template>
                        </el-table-column>
                        <el-table-column label="已被分配的数量" width="120" align="center" header-align="center">
                            <template slot-scope="scope">
                                {{scope.row.id|getCount}}
                            </template>
                        </el-table-column>
                        <el-table-column prop="remarks" label="描述" align="center"/>
                    </el-table>
                </div>
            </el-card>
            <el-row>
                <el-col :span="4" offset="22">
                    <el-button  type="primary" size="big" @click="allocation" >分 配</el-button>
                </el-col>
            </el-row>

        </div>

    </section>
</template>

<script>
    import indexMixin from '@dr/auto/lib/util/indexMixin'

    export default {
        data() {
            return {
                organiseId:'',
                label:'',
                houseForm:{},
                parkList:[],
                builds:[],
                floors:[],
                multipleSelection: [],
            }
        },
        mixins: [indexMixin],
        methods: {
            $init() {
                this.organiseId = this.$route.query.organiseId
                this.label = this.$route.query.label
                this.loadData()
                this.getParks()
                this.getParkList()
                this.getBuildList()
                this.getCountList()
            },
            getParks(){
                this.loading = true
                this.$http.post('park/getAllPark').then(({data}) => {
                    if (data.success) {
                        this.parkList = data.data
                    }
                    this.loading = false
                })
            },
            loadData (params) {
                if(params == '' || params == undefined){
                    params = {page:false}
                }
                this.loading = true
                this.$http.post('room/page', params).then(({data}) => {
                    if (data && data.success) {
                        this.data = data.data
                    }
                    this.loading = false
                })
            },
            detail(data){
                this.$router.push({path: '/assets/room/detail', query: {roomId: data.id}})
            },
            getBuildByPark(){
                this.loading = true
                this.$http.post('building/getBuildListByParkId', {parkId: this.houseForm.park})
                    .then(({data}) => {
                        if (data.success) {
                            this.builds = data.data
                        }
                        this.loading = false
                    })
            },
            getFloorByBuild(){
                this.loading = true
                this.$http.post('floor/getFloorListByBuildingId', {buildingId: this.houseForm.building})
                    .then(({data}) => {
                        if (data.success) {
                            this.floors = data.data
                        }
                        this.loading = false
                    })
            },
            searchRoom(){
                let params = {
                    parkId: this.houseForm.park,
                    buildingID: this.houseForm.building,
                    foolrId: this.houseForm.floor,
                    page:false
                }
                this.loadData(params)
            },
            resetRoom(){
                this.houseForm.park = ''
                this.houseForm.building = ''
                this.houseForm.floor = ''
            },
            checkSelectable(row) {
                return row.organise == null
            },
            allocation(){
                this.$confirm('确定将这些房间分配给“'+this.label+'”部门吗?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    var ids = ''
                    this.selectIds.filter(val => {
                        ids = ids  + val + ","
                    })
                    this.$http.post('/room/allocationRoomToOrg', {
                        roomIds: ids,
                        orgId: this.organiseId
                    }).then(({data}) => {
                        if (data.success) {
                            this.$message.success("分配成功！")
                            this.loadData()
                        } else {
                            this.$message.error(data.message)
                        }
                        this.loading = false
                    })
                })
            },
        }
    }
</script>

