package com.gabriel.ferreira.ms_customer.application.service;

import com.gabriel.ferreira.ms_customer.application.interfaces.IAddressService;
import com.gabriel.ferreira.ms_customer.application.interfaces.ICustomerService;
import com.gabriel.ferreira.ms_customer.domain.enums.Sex;
import com.gabriel.ferreira.ms_customer.domain.model.address.response.AddressResponse;
import com.gabriel.ferreira.ms_customer.domain.model.customer.Customer;
import com.gabriel.ferreira.ms_customer.domain.model.customer.request.CustomerRequest;
import com.gabriel.ferreira.ms_customer.domain.model.customer.response.CustomerResponse;
import com.gabriel.ferreira.ms_customer.domain.repository.ICustomerRepository;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class CustomerService implements ICustomerService {
    private final ICustomerRepository _customerRepository;
    private final IAddressService _addressService;
    private final ModelMapper _modelMapper;


    public CustomerService(ICustomerRepository customerRepository, IAddressService addressService, ModelMapper modelMapper) {
        _customerRepository = customerRepository;
        _addressService = addressService;
        _modelMapper = modelMapper;
    }

    @Override
    public CustomerResponse criarCustomer(CustomerRequest customerRequest) {
        validarAtributosCustomer(customerRequest);
        validarSeEmailJaExiste(customerRequest.getEmail());
        validarSeCpfJaExiste(customerRequest.getCpf());
        Customer customer = _modelMapper.map(customerRequest, Customer.class);
        return _modelMapper.map(_customerRepository.save(customer), CustomerResponse.class);
    }

    @Override
    public CustomerResponse buscarCustomerPorId(Integer customerId) {
        Customer customer = _customerRepository.findById(customerId).orElseThrow(
                () -> new RuntimeException("Customer não encontrado"));

        List<AddressResponse> addressResponses = _addressService.buscarAddressesPorCustomerId(customer.getId());

        CustomerResponse customerResponse = _modelMapper.map(customer, CustomerResponse.class);
        customerResponse.setAddresses(addressResponses);

        return customerResponse;
    }

    @Override
    public CustomerResponse atualizarCustomer(CustomerRequest customerRequest, Integer customerId) {
        validarAtributosCustomer(customerRequest);
        Customer customer = _customerRepository.findById(customerId).orElseThrow(
                () -> new RuntimeException("Customer não encontrado")
        );
        Optional.ofNullable(customerRequest.getFirstName()).filter(firstName -> !firstName.isEmpty()).ifPresent(customer::setFirstName);
        Optional.ofNullable(customerRequest.getLastName()).filter(lastName -> !lastName.isEmpty()).ifPresent(customer::setLastName);
        Optional.ofNullable(customerRequest.getCpf()).filter(cpf -> !cpf.isEmpty()).ifPresent(customer::setCpf);
        Optional.ofNullable(customerRequest.getEmail()).filter(email -> !email.isEmpty()).ifPresent(customer::setEmail);
        Optional.ofNullable(customerRequest.getBirthdate()).ifPresent(customer::setBirthdate);
        Optional.ofNullable(customerRequest.getSex()).ifPresent(customer::setSex);
        Optional.ofNullable(customerRequest.getPassword()).filter(password -> !password.isEmpty()).ifPresent(customer::setPassword);
        Optional.ofNullable(customerRequest.getActive()).ifPresent(customer::setActive);

        return _modelMapper.map(customer, CustomerResponse.class);

    }

    private static void validarAtributosCustomer(CustomerRequest customerRequest){
        boolean customerCamposVazios = customerRequest.getFirstName().isEmpty() || customerRequest.getLastName().isEmpty()
                || customerRequest.getCpf().isEmpty() || customerRequest.getEmail().isEmpty()
                || customerRequest.getPassword().isEmpty() || customerRequest.getBirthdate() == null
                || customerRequest.getSex() == null || customerRequest.getActive() == null;

        if (customerCamposVazios){
            throw new RuntimeException("Campos Vazios");
        }
        if(customerRequest.getFirstName().length() < 3){
            throw new RuntimeException("< 3 firstName");
        }
        if(customerRequest.getLastName().length() < 3){
            throw new RuntimeException("< 3 lastName");
        }
        boolean customerSexValido = customerRequest.getSex().equals(Sex.Feminino) || customerRequest.getSex().equals(Sex.Masculino) ;
        if (!customerSexValido){
            throw new RuntimeException("Sexo incorreto");
        }
        if(customerRequest.getPassword().length() < 6){
            throw new RuntimeException("Senha < 6");
        }
        validarCpf(customerRequest.getCpf());
        validarEmail(customerRequest.getEmail());
    }
    private void validarSeEmailJaExiste(String email) {
        Optional<Customer> customer = _customerRepository.findByEmail(email);
        if(customer.isPresent()){
            throw new RuntimeException("Customer já existe com esse email");
        }
    }
    private void validarSeCpfJaExiste(String cpf) {
        Optional<Customer> customer = _customerRepository.findByCpf(cpf);
        if(customer.isPresent()){
            throw new RuntimeException("Customer já existe com esse CPF");
        }
    }
    private static void validarCpf(String cpf){


        if(cpf.length() != 14){
            throw new RuntimeException("Cpf inválido 1 ");
        }

        CPFValidator validator = new CPFValidator();
        validator.initialize(null);
        if(!validator.isValid(cpf, null)){
            throw new RuntimeException("Cpf inválido");
        }
    }
    private static void validarEmail(String email){
        String regex = "^[\\w+.\\-]+@[\\w+.-]+\\.[\\w]{2,}$";
        if (!Pattern.matches(regex, email)){
            throw new RuntimeException("Email invalido");
        }
    }
}
