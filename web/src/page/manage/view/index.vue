<template>
  <div class="detail">
    <h4 class="title">接口详情 &nbsp;&nbsp;&nbsp;&nbsp;
      <el-button v-if="versioned.length > 0" type="primary" circle icon="el-icon-timer" title="查看历史版本" size="small" @click="listVersioned"></el-button></h4>
    <hr/>
    <el-form v-if="data" label-width="9em">
      <el-form-item label="接口名称:">
        <div>{{data.name}}<b v-if="data.deprecated" style="color:red">（已废弃）</b></div>
        <div class="history" v-if="currentVersioned">{{currentVersioned.name}}<b v-if="currentVersioned.deprecated" style="color:red">（已废弃）</b></div>
      </el-form-item>
      <el-form-item label="控制器:">
        <div>{{data.controller}}</div>
        <div class="history" v-if="currentVersioned">{{currentVersioned.controller}}</div>
      </el-form-item>
      <el-form-item label="访问类型:">
        <div>{{data.method}}</div>
        <div class="history" v-if="currentVersioned">{{currentVersioned.method}}</div>
      </el-form-item>
      <el-form-item label="所属项目:">
        <div>{{data.end.name}}</div>
        <div class="history" v-if="currentVersioned">{{currentVersioned.end.name}}</div>
      </el-form-item>
      <el-form-item label="header参数说明:">
        <div><i style="color:red;">header名:{{data.end.header}}<br>{{data.end.mark}}</i></div>
        <div class="history" v-if="currentVersioned"><i>header名:{{currentVersioned.end.header}}<br>{{currentVersioned.end.mark}}</i></div>
      </el-form-item>
      <el-form-item label="所属模块:">
        <div>{{data.java.name}}</div>
        <div class="history" v-if="currentVersioned">{{currentVersioned.java.name}}</div>
      </el-form-item>
      <el-form-item label="开发环境地址:"><a @click="linkTo(data, 'devDomain')" target="_blank">{{`${data.java.devDomain}${data.url}`}}</a></el-form-item>
      <el-form-item label="测试环境地址:"><a @click="linkTo(data, 'testDomain')" target="_blank">{{`${data.java.testDomain}${data.url}`}}</a></el-form-item>
      <el-form-item label="预发环境地址:"><a @click="linkTo(data, 'prevDomain')" target="_blank">{{`${data.java.prevDomain}${data.url}`}}</a></el-form-item>
      <el-form-item label="线上环境地址:"><a @click="linkTo(data, 'onlineDomain')" target="_blank">{{`${data.java.onlineDomain}${data.url}`}}</a></el-form-item>
      <el-form-item label="创建时间:">{{formatDate(data.createTime)}}</el-form-item>
      <el-form-item label="更新时间:">{{formatDate(data.modifyTime)}}</el-form-item>
      <el-form-item label="接口参数:">
        <v-parameter :json="parameters || []"></v-parameter>
        <v-parameter class="history" v-if="currentVersioned" :json="versionedParameters || []"></v-parameter>
      </el-form-item>
      <el-form-item label="接口说明:">
        <div v-html="(data.mark || '无')"></div>
        <div class="history" v-if="currentVersioned" v-html="(currentVersioned.mark || '无')"></div>
      </el-form-item>
      <el-form-item label="返回值:">
        <div v-if="currentVersioned">
          <v-jsonformatter v-if="generatedReturns" :json="generatedReturns" style="width: 48%;display: inline-block;"></v-jsonformatter>
          <v-jsonformatter class="history" v-if="currentVersioned && versionedGeneratedReturns" style="width: 48%;display: inline-block;"
                           :json="versionedGeneratedReturns"></v-jsonformatter>
        </div>
        <div v-else>
          <v-jsonformatter v-if="generatedReturns" :json="generatedReturns"></v-jsonformatter>
        </div>
      </el-form-item>
      <!--<el-form-item label="返回值数据结构:">-->
        <!--<v-jsonformatter :json="returns"></v-jsonformatter>-->
      <!--</el-form-item>-->
    </el-form>
    <v-dialog ref="historyDialog"
              title="选择一个历史版本进行对比"
              :ok="claimVersioned">
      <div slot="body">
        <v-datagrid ref="dg" :loadData="versioned" :checkable="true" :singleCheck="true" :columns="columns" :pageable="false"/>
      </div>
    </v-dialog>
  </div>
</template>

<script>
import { getInterfacesApi, getInterfacesVersionedApi } from '@/config/api/inserv-api'
import { formatDate } from '@/config/utils'

export default {
  name: 'interfacesDetail',
  data () {
    return {
      data: null,
      formatDate,
      returns: null,
      parameters: null,
      generatedReturns: null,
      versioned: [],
      currentVersioned: null,
      columns: [
        {field: 'deprecated', header: '废弃', sort: 'deprecated', html: true, width: 80, formatter: (row, index, value) => value ? '是'.fontcolor('red') : '否'},
        {field: 'url', header: 'URL地址', sort: 'url', width: 220},
        {field: 'modifyTime', header: '上次更新时间', sort: 'modify_time', width: 200, formatter: (row, index, value) => formatDate(value)}
      ]
    }
  },
  computed: {
  },
  methods: {
    claimVersioned () {
      const current = this.$refs.dg.getCheckedData()
      if (current != null && current.length > 0) {
        this.currentVersioned = current[0]

        this.versionedReturns = JSON.parse(this.currentVersioned.returns)
        this.versionedGeneratedReturns = this.generateReturns(this.versionedReturns)
        this.versionedParameters = JSON.parse(this.currentVersioned.parameters)
      } else {
        this.currentVersioned = null
      }
      this.$refs.historyDialog.close()
    },
    listVersioned () {
      this.$refs.historyDialog.open()
    },
    init () {
      getInterfacesApi(this.$route.params.id).then((res) => {
        this.data = res
        this.returns = JSON.parse(this.data.returns)
        this.generatedReturns = this.generateReturns(this.returns)
        this.parameters = JSON.parse(this.data.parameters)
      })

      getInterfacesVersionedApi(this.$route.params.id).then((res) => {
        this.versioned = res || []
      })
    },
    linkTo (data, env) {
      this.$router.push(`/test/${data.id}/${env}`)
    },
    generateReturns (node) {
      if (node.type === 'List') {
        return [this.generateReturns(node.children[0])]
      } else if (node.type === 'Object') {
        const o = {}
        node.children = node.children || []
        node.children.forEach(s => {
          o[s.key] = this.generateReturns(s)
        })
        return o
      } else if (node.type === undefined) {
        return '...'
      } else if (node.type === 'void') {
        return '无'
      } else {
        return `${node.comment ? node.comment + '； ' : ''} 数据类型：${node.type}； ${node.notNull ? '此项一定不为空；' : ''}`
      }
    }
  },
  components: {
    'v-datagrid': () => import('@/components/datagrid'),
    'v-jsonformatter': () => import('@/components/jsonformatter'),
    'v-parameter': () => import('@/components/parameter'),
    'v-dialog': () => import('@/components/dialog')
  },
  mounted () {
    this.init()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style type="text/css" lang="scss" scoped>
  @import 'index.scss';
</style>
