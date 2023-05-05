package br.com.ada.testeautomatizado.service;

import br.com.ada.testeautomatizado.dto.VeiculoDTO;
import br.com.ada.testeautomatizado.exception.PlacaInvalidaException;
import br.com.ada.testeautomatizado.exception.VeiculoNaoEncontradoException;
import br.com.ada.testeautomatizado.model.Veiculo;
import br.com.ada.testeautomatizado.repository.VeiculoRepository;
import br.com.ada.testeautomatizado.util.Response;
import br.com.ada.testeautomatizado.util.ValidacaoPlaca;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.naming.NotContextException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ValidacaoPlaca validacaoPlaca;

    public ResponseEntity< Response< VeiculoDTO > > cadastrar( VeiculoDTO veiculoDTO ) {
        try
        {
            log.trace("Dados veiculoDTO {}", veiculoDTO.toString());

            this.validacaoPlaca.isPlacaValida( veiculoDTO.getPlaca( ) );
            Veiculo veiculo = new Veiculo( );
            veiculo.setPlaca( veiculoDTO.getPlaca( )                   );
            veiculo.setModelo( veiculoDTO.getModelo( )                 );
            veiculo.setMarca( veiculoDTO.getMarca( )                   );
            veiculo.setDisponivel( veiculoDTO.getDisponivel( )         );
            veiculo.setDataFabricacao( veiculoDTO.getDataFabricacao( ) );

            this.veiculoRepository.save( veiculo );
            log.debug( "Cadastrado com sucesso" );
            return ResponseEntity.ok(Response.<VeiculoDTO>builder().message("Sucesso").detail(veiculoDTO).build());

        } catch ( VeiculoNaoEncontradoException | PlacaInvalidaException e )
        {
             return ResponseEntity.status( HttpStatus.UNPROCESSABLE_ENTITY )
                     .body( new Response< VeiculoDTO >( e.getMessage(), null));

        } catch ( Exception e )
        {
            log.error( "Erro ao cadastrar o veículo {} ", e.getMessage( ) );
            throw e;
        }
    }

    public ResponseEntity< Response< Boolean > > deletarVeiculoPelaPlaca(String placa) {
        try
        {
         buscarVeiculoPelaPlaca( placa ).ifPresent( this.veiculoRepository::delete );
         Response< Boolean > response = new Response<>( "Sucesso", Boolean.TRUE );
         return ResponseEntity.ok(response);
        } catch ( VeiculoNaoEncontradoException e ) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    public ResponseEntity<Response<VeiculoDTO>> atualizar(VeiculoDTO veiculoDTO) {
        try
        {
            this.validacaoPlaca.isPlacaValida( veiculoDTO.getPlaca( ) );

            Optional< Veiculo > optionalVeiculo = this.veiculoRepository.findByPlaca( veiculoDTO.getPlaca( ) );
            if ( optionalVeiculo.isPresent( ) )
            {
                Veiculo veiculo = new Veiculo( );
                veiculo.setId( optionalVeiculo.get( ).getId( )             );
                veiculo.setPlaca( veiculoDTO.getPlaca( )                   );
                veiculo.setModelo( veiculoDTO.getModelo( )                 );
                veiculo.setMarca( veiculoDTO.getMarca( )                   );
                veiculo.setDisponivel( veiculoDTO.getDisponivel( )         );
                veiculo.setDataFabricacao( veiculoDTO.getDataFabricacao( ) );

                Veiculo veiculoAtualizadoNoBD = this.veiculoRepository.save( veiculo );

                VeiculoDTO veiculoDTOAtualizado = new VeiculoDTO( );
                veiculoDTOAtualizado.setPlaca( veiculoAtualizadoNoBD.getPlaca( )                   );
                veiculoDTOAtualizado.setModelo( veiculoAtualizadoNoBD.getModelo( )                 );
                veiculoDTOAtualizado.setMarca( veiculoAtualizadoNoBD.getMarca( )                   );
                veiculoDTOAtualizado.setDisponivel( veiculoAtualizadoNoBD.getDisponivel( )         );
                veiculoDTOAtualizado.setDataFabricacao( veiculoAtualizadoNoBD.getDataFabricacao( ) );

                Response< VeiculoDTO > response = new Response< >( "Sucesso", veiculoDTOAtualizado);

                return ResponseEntity.ok( response );
            } else
            {
                throw new VeiculoNaoEncontradoException();
            }
        } catch ( VeiculoNaoEncontradoException e )
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch ( PlacaInvalidaException e )
        {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new Response< >( e.getMessage(), null));
        } catch ( Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<Response<List<VeiculoDTO>>> listarTodos( ) {
        try
        {
            List< VeiculoDTO> listaVeiculos = veiculoRepository.findAll( ).stream( ).map( veiculo -> {
                VeiculoDTO veiculoDTO = new VeiculoDTO( );
                veiculoDTO.setPlaca( veiculo.getPlaca( )                   );
                veiculoDTO.setModelo( veiculo.getModelo( )                 );
                veiculoDTO.setMarca( veiculo.getMarca( )                   );
                veiculoDTO.setDisponivel( veiculo.getDisponivel( )         );
                veiculoDTO.setDataFabricacao( veiculo.getDataFabricacao( ) );
                return veiculoDTO;
            } ).collect( Collectors.toList( ) );
            Response< List < VeiculoDTO > > response = new Response< > ( "Sucesso", listaVeiculos);
            return ResponseEntity.ok( response );

        } catch ( VeiculoNaoEncontradoException e )
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

    }

    private Optional<Veiculo> buscarVeiculoPelaPlaca(String placa) {
        return this.veiculoRepository.findByPlaca(placa);
    }
}

