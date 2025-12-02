package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.ClienteDTO;
import com.oficinamecanica.OficinaMecanica.models.ClienteModel;
import com.oficinamecanica.OficinaMecanica.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteDTO criar(ClienteDTO dto) {
        log.info("Criando cliente: {}", dto.nmCliente());

        if (dto.cpf() != null && clienteRepository.existsByCpf(dto.cpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        if (dto.email() != null && clienteRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        ClienteModel cliente = ClienteModel.builder()
                .nmCliente(dto.nmCliente())
                .cpf(dto.cpf())
                .telefone(dto.telefone())
                .email(dto.email())
                .endereco(dto.endereco())
                .ativo(true)
                .build();

        ClienteModel salvo = clienteRepository.save(cliente);

        log.info("Cliente criado: ID {} - {}", salvo.getCdCliente(), salvo.getNmCliente());

        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Integer id) {
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        return converterParaDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarAtivos() {
        log.info("Listando clientes ativos");

        return clienteRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorCpf(String cpf) {
        ClienteModel cliente = clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return converterParaDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorNome(String nome) {
        return clienteRepository.findByNmClienteContainingIgnoreCase(nome).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO atualizar(Integer id, ClienteDTO dto) {
        log.info("Atualizando cliente ID: {}", id);

        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getCpf().equals(dto.cpf()) &&
                clienteRepository.existsByCpf(dto.cpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        if (dto.email() != null &&
                !dto.email().equals(cliente.getEmail()) &&
                clienteRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        cliente.setNmCliente(dto.nmCliente());
        cliente.setCpf(dto.cpf());
        cliente.setTelefone(dto.telefone());
        cliente.setEmail(dto.email());
        cliente.setEndereco(dto.endereco());

        ClienteModel atualizado = clienteRepository.save(cliente);

        log.info("Cliente atualizado: {}", atualizado.getNmCliente());

        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        log.info("Deletando cliente ID: {}", id);

        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        cliente.setAtivo(false);
        clienteRepository.save(cliente);

        log.info("Cliente marcado como inativo");
    }

    private ClienteDTO converterParaDTO(ClienteModel cliente) {
        return new ClienteDTO(
                cliente.getCdCliente(),
                cliente.getNmCliente(),
                cliente.getCpf(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.getAtivo()
        );
    }
}