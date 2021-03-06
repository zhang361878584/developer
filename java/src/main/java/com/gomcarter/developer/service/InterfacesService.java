package com.gomcarter.developer.service;

import com.alibaba.nacos.client.config.utils.MD5;
import com.gomcarter.developer.dao.InterfacesMapper;
import com.gomcarter.developer.entity.End;
import com.gomcarter.developer.entity.Interfaces;
import com.gomcarter.developer.entity.Java;
import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.base.common.CollectionUtils;
import com.gomcarter.frameworks.base.exception.CustomException;
import com.gomcarter.frameworks.base.mapper.JsonMapper;
import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.interfaces.dto.ApiInterface;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author gomcarter
 * @date 2019-06-17 16:41:01
 */
@Service
public class InterfacesService {

    @Autowired
    private InterfacesMapper interfacesMapper;

    @Autowired
    private JavaService javaService;

    @Autowired
    private EndService endService;

    @Autowired
    private InterfacesVersionedService interfacesVersionedService;

    public void insert(Interfaces interfaces) {
        interfacesMapper.insert(interfaces);
    }

    public void update(Interfaces interfaces) {
        interfacesMapper.update(interfaces);
    }

    public Interfaces getById(Long id) {
        return interfacesMapper.getById(id);
    }

    public List<Interfaces> getByIdList(Collection<Long> idList) {
        return interfacesMapper.getByIdList(idList);
    }

    public <R> List<Interfaces> query(R params, Pageable pager) {
        return interfacesMapper.query(params, pager);
    }

    public <R> Integer count(R params) {
        return interfacesMapper.count(params);
    }

    public Integer insert(Long javaId, List<ApiInterface> interfaceList) {
        if (CollectionUtils.isEmpty(interfaceList)) {
            return 0;
        }

        Java java = javaService.getById(javaId);
        AssertUtils.notNull(java, new CustomException("java项目不正确"));

        // 先把所有接口置为废弃。
        this.interfacesMapper.setDeprecatedByJavaId(javaId);

        Integer success = 0;
        for (ApiInterface s : interfaceList) {
            String url = s.getUrl();
            String prefix = Arrays.stream(url.split("/"))
                    .filter(StringUtils::isNotBlank)
                    .findFirst()
                    .orElse(null);

            End end = endService.getByPrefix(prefix);
            if (end == null) {
                end = endService.insertOrGetDefault();
            }

            String returns = JsonMapper.buildNonNullMapper().toJson(s.getReturns());
            String parameters = JsonMapper.buildNonNullMapper().toJson(s.getParameters());
            String hash = MD5.getInstance().getMD5String(
                    StringUtils.join(new String[]{
                            url, javaId.toString(), end.getId().toString(), s.isDeprecated() + "",
                            s.getMark(), s.getMethod(), s.getName(), returns, parameters, s.getController()
                    }, ","));

            Interfaces interfaces = this.interfacesMapper.getByUrl(url, s.getMethod());
            // 如果接口没有发生变化，那么对应的hash就是一样的，应该不插入这个接口
            if (interfaces == null) {
                interfaces = new Interfaces()
                        .setUrl(url)
                        .setController(s.getController())
                        .setFkJavaId(javaId)
                        .setHash(hash)
                        .setDeprecated(s.isDeprecated())
                        .setFkEndId(end.getId())
                        .setMark(s.getMark())
                        .setMethod(s.getMethod())
                        .setName(s.getName())
                        .setReturns(returns)
                        .setParameters(parameters);

                this.insert(interfaces);
                success++;
            } else if (!hash.equals(interfaces.getHash())) {
                // 插入历史版本
                this.interfacesVersionedService.insert(interfaces, s.isDeprecated());

                // 已经存在接口，那么看hash是否改变，改变了就修改，没改变就不修改
                this.update(interfaces.setUrl(url)
                        .setFkJavaId(javaId)
                        .setHash(hash)
                        .setController(s.getController())
                        .setDeprecated(s.isDeprecated())
                        .setFkEndId(end.getId())
                        .setMark(s.getMark())
                        .setMethod(s.getMethod())
                        .setName(s.getName())
                        .setReturns(returns)
                        .setParameters(parameters));

                success++;
            } else {
                // 接口没有任何变化
                this.update(interfaces.setDeprecated(s.isDeprecated()));
            }
        }
        return success;
    }

    public Interfaces getByHash(String hash) {
        return this.interfacesMapper.getByHash(hash);
    }

    public void delete(Long id) {
        this.interfacesMapper.deleteById(id);
    }
}
