package com.gomcarter.developer.controller.developer;

import com.gomcarter.developer.dto.JEnd;
import com.gomcarter.developer.dto.JInterfaces;
import com.gomcarter.developer.dto.JInterfacesDetail;
import com.gomcarter.developer.dto.JJava;
import com.gomcarter.developer.entity.End;
import com.gomcarter.developer.entity.Interfaces;
import com.gomcarter.developer.entity.Java;
import com.gomcarter.developer.params.JArgs;
import com.gomcarter.developer.params.JInterfacesQueryParam;
import com.gomcarter.developer.service.EndService;
import com.gomcarter.developer.service.InterfacesService;
import com.gomcarter.developer.service.JavaService;
import com.gomcarter.frameworks.base.annotation.IgnoreLogin;
import com.gomcarter.frameworks.base.common.CollectionUtils;
import com.gomcarter.frameworks.base.mapper.JsonMapper;
import com.gomcarter.frameworks.base.pager.DefaultPager;
import com.gomcarter.frameworks.base.streaming.Streamable;
import com.gomcarter.frameworks.interfaces.annotation.Notes;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gomcarter
 */
@RestController
@RequestMapping("developer/interfaces")
public class DeveloperInterfacesController {

    @Autowired
    private InterfacesService interfacesService;

    @Autowired
    private EndService endService;

    @Autowired
    private JavaService javaService;

    /**
     * 自动生成鉴权 token，配置好鉴权的 jar 包地址，通过反射调用 jar 包里面的生成鉴权的 token 返回到前台
     *
     * @param id interfaces id
     * @return Pair
     * @throws Exception Exception
     */
    @GetMapping(value = "headers/{id}", name = "自动生成headers")
    public Pair headers(@PathVariable Long id) throws Exception {
        Interfaces interfaces = this.interfacesService.getById(id);
        End end = this.endService.getById(interfaces.getFkEndId());
        if (StringUtils.isBlank(end.getJarUrl())) {
            return null;
        }
        URL url = new URL(end.getJarUrl());
        URLClassLoader myClassLoader1 = new URLClassLoader(new URL[]{url}, Thread.currentThread().getContextClassLoader());
        Class<?> kls = myClassLoader1.loadClass(end.getKls());
        List<JArgs> argsList = JsonMapper.buildNonNullMapper().fromJsonToList(end.getArgs(), JArgs.class);

        Class[] classes = new Class[argsList.size()];
        int i = 0;
        for (JArgs args : argsList) {
            classes[i++] = args.getKey();
        }

        Method method = kls.getMethod(end.getMethod(), classes);
        return new Pair<>(end.getHeader(), method.invoke(null, argsList.stream().map(JArgs::getValue).toArray()));
    }

    @PostMapping(value = "delete/{id}", name = "删除接口")
    public void delete(@PathVariable Long id) {
        this.interfacesService.delete(id);
    }


    @GetMapping(value = "list", name = "获取接口地址列表")
    public List<JInterfaces> list(@Notes("查询参数") JInterfacesQueryParam params, @Notes("分页器") DefaultPager pager) {
        List<Interfaces> interfacesList = interfacesService.query(params, pager);
        if (CollectionUtils.isEmpty(interfacesList)) {
            return new ArrayList<>();
        }

        Map<Long, Java> javaMap = Streamable.valueOf(javaService.getByIdList(interfacesList.stream().map(Interfaces::getFkJavaId).collect(Collectors.toSet())))
                .uniqueGroupby(Java::getId)
                .collect();

        Map<Long, End> endMap = Streamable.valueOf(endService.getByIdList(interfacesList.stream().map(Interfaces::getFkEndId).collect(Collectors.toSet())))
                .uniqueGroupby(End::getId)
                .collect();

        return interfacesList.stream()
                .map(s -> new JInterfaces()
                        .setId(s.getId())
                        .setHash(s.getHash())
                        .setName(s.getName())
                        .setUrl(s.getUrl())
                        .setController(s.getController())
                        .setMethod(s.getMethod())
                        .setMark(s.getMark())
                        .setJava(javaMap.get(s.getFkJavaId()).getName())
                        .setEnd(endMap.get(s.getFkEndId()).getName())
                        .setDeprecated(s.getDeprecated())
                        .setCreateTime(s.getCreateTime())
                        .setModifyTime(s.getModifyTime())
                )
                .collect(Collectors.toList());
    }

    @GetMapping(value = "{id}", name = "获取接口详情")
    @IgnoreLogin
    public JInterfacesDetail detail(@Notes("查询参数") @PathVariable Long id) {
        Interfaces interfaces = interfacesService.getById(id);

        Java java = javaService.getById(interfaces.getFkJavaId());
        End end = endService.getById(interfaces.getFkEndId());

        return new JInterfacesDetail()
                .setId(interfaces.getId())
                .setController(interfaces.getController())
                .setHash(interfaces.getHash())
                .setName(interfaces.getName())
                .setUrl(interfaces.getUrl())
                .setMethod(interfaces.getMethod())
                .setReturns(interfaces.getReturns())
                .setParameters(interfaces.getParameters())
                .setMark(interfaces.getMark())
                .setJava(new JJava()
                        .setId(java.getId())
                        .setName(java.getName())
                        .setDevDomain(java.getDevDomain())
                        .setTestDomain(java.getTestDomain())
                        .setPrevDomain(java.getPrevDomain())
                        .setOnlineDomain(java.getOnlineDomain())
                )
                .setEnd(new JEnd()
                        .setId(end.getId())
                        .setName(end.getName())
                        .setHeader(end.getHeader())
                        .setPrefix(end.getPrefix())
                        .setMark(end.getMark())
                )
                .setDeprecated(interfaces.getDeprecated())
                .setCreateTime(interfaces.getCreateTime())
                .setModifyTime(interfaces.getModifyTime());
    }

    @GetMapping(value = "count", name = "获取接口地址列表总数")
    public Integer count(@Notes("查询参数") JInterfacesQueryParam params) {
        return interfacesService.count(params);
    }
}
