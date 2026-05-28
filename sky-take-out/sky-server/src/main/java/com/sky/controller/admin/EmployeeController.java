package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 * 管理端发出来的请求统一用/admin前缀
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO;
     * @return Result.success(employeeLoginVO);
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        //登录校验
        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return Result.success();
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     *
     * @param employeeDTO;
     * @return Result.success();
     */
    @PostMapping
    public Result<Void> save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     *
     * 员工分页查询
     *
     * @param employeePageQueryDTO;
     * @return Result.success();
     */
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询，参数为：{}", employeePageQueryDTO);

        PageResult pr = employeeService.page(employeePageQueryDTO);

        return Result.success(pr);
    }

    /**
     *
     * 启用禁用员工账号
     *
     * @param status,id;
     * @return Result.success();
     */
    @PostMapping("/status/{status}")
    public Result<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("启用禁用员工账号：{}，{}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     *
     * 根据员工id查询信息
     *
     * @param id;
     * @return Result.success(employee);
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("要查询的员工的id为：{}", id);
        Employee employee = employeeService.getById(id);
        employee.setPassword("****");

        return Result.success(employee);
    }

    /**
     *
     * 编辑员工信息
     *
     * @param employeeDTO;
     * @return Result.success();
     *
     *
     */
    @PutMapping
    public Result<Void> update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息：{}", employeeDTO);
        employeeService.update(employeeDTO);

        return Result.success();
    }

    /**
     * 修改密码
     *
     * @param passwordEditDTO ;
     * @return Result.success();
     *
     */
    @PutMapping("/editPassword")
    public Result<Void> updatePassWord(@RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("修改员工密码：{}", passwordEditDTO);
        employeeService.updatePassWord(passwordEditDTO);

        return Result.success();
    }
}
