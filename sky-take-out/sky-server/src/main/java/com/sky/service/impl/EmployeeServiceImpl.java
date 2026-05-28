package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端传过来的密码明文进行加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     *
     * 新增员工
     *
     * @param employeeDTO;
     *
     *
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //使用employee而不是employeeDTO是因为有些和数据库相关的值employeeDTO没有
        Employee employee = new Employee();

        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置账号的状态 默认正常状态，0锁定，1正常
        employee.setStatus(StatusConstant.ENABLE);

        //设置账号的密码，默认密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        employeeMapper.insert(employee);
    }


    /**
     *
     * 员工分页查询
     *
     * @param employeePageQueryDTO;
     *
     */
    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        //select * from employee limit 0,10
        //开始分页查询
        //该方法相当于在后面的sql语句加上了limit
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();

        //返回的是装了Employee类的集合
        List<Employee> result = page.getResult();

        return new PageResult(total, result);
    }

    /**
     *
     * 启用禁用员工账号
     *
     * @param status,id;
     *
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //update employee set status=? where id=#{id};
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);

        employeeMapper.update(employee);
    }

    /**
     *
     * 根据员工id查询信息
     *
     * @param id;
     * @return employee;
     */
    @Override
    public Employee getById(Long id) {
        return employeeMapper.getById(id);
    }

    /**
     *
     * 编辑员工信息
     *
     * @param employeeDTO;
     *
     *
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        employeeMapper.update(employee);
    }

    /**
     *
     * 修改密码
     *
     * @param passwordEditDTO ;
     *
     */
    @Override
    public void updatePassWord(PasswordEditDTO passwordEditDTO) {
        //拿出使用者id
        Long empId = BaseContext.getCurrentId();

        //使用者老密码
        String oldPassword = passwordEditDTO.getOldPassword();

        //使用者新密码
        String newPassword = passwordEditDTO.getNewPassword();

        Employee employee = employeeMapper.getById(empId);

        //数据库里使用者旧密码
        String password = employee.getPassword();

        //加密输入的旧密码，判断和对象的旧密码是否一致
        oldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());

        //如果不一致，代表输入旧密码错误
        if (!password.equals(oldPassword)) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //输入的旧密码和对象的旧密码一致,加密输入的新密码判断和输入的旧密码是否一致
        newPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        if (oldPassword.equals(newPassword)) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }

        //输入的新旧密码不一样，更新对象的密码
        employee.setPassword(newPassword);

        //更新数据库
        employeeMapper.update(employee);
    }

}
