package ma.projet.grpc.controllers;

import io.grpc.stub.StreamObserver;
import ma.projet.grpc.services.CompteService;
import ma.projet.grpc.stubs.*;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    // Simuler une base de données en mémoire
    private final Map<String, Compte> compteDB = new ConcurrentHashMap<>();

    private final CompteService compteService;

    public CompteServiceImpl(final CompteService compteService) {
        this.compteService = compteService;
}
    @Override
    public void allComptes(GetAllComptesRequest request, StreamObserver<GetAllComptesResponse> responseObserver) {
        var comptes = compteService.findAllComptes().stream()
                .map(compte -> Compte.newBuilder()
                        .setId(compte.getId())
                        .setSolde(compte.getSolde())
                        .setDateCreation(compte.getDateCreation())
                        .setType(TypeCompte.valueOf(compte.getType()))
                        .build())
                .collect(Collectors.toList());

        responseObserver.onNext(GetAllComptesResponse.newBuilder().addAllComptes(comptes).build());
        responseObserver.onCompleted();

}

    @Override
    public void compteById(GetCompteByIdRequest request, StreamObserver<GetCompteByIdResponse> responseObserver) {
        Compte compte = compteDB.get(request.getId());
        if (compte != null) {
            responseObserver.onNext(GetCompteByIdResponse.newBuilder().setCompte(compte).build());
        } else {
            responseObserver.onError(new Throwable("Compte non trouvé"));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void totalSolde(GetTotalSoldeRequest request, StreamObserver<GetTotalSoldeResponse> responseObserver) {
        int count = compteDB.size();
        float sum = 0;
        for (Compte compte : compteDB.values()) {
            sum += compte.getSolde();
        }
        float average = count > 0 ? sum / count : 0;

        SoldeStats stats = SoldeStats.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAverage(average)
                .build();

        responseObserver.onNext(GetTotalSoldeResponse.newBuilder().setStats(stats).build());
        responseObserver.onCompleted();
    }

    @Override
    public void saveCompte(SaveCompteRequest request, StreamObserver<SaveCompteResponse> responseObserver) {
        var compteReq = request.getCompte();
        var compte = new ma.projet.grpc.entities.Compte();

        compte.setSolde(compteReq.getSolde());
        compte.setDateCreation(compteReq.getDateCreation());
        compte.setType(compteReq.getType().name());

        // Save to the database
        var savedCompte = compteService.saveCompte(compte);

        // Map to GRPC response
        var grpcCompte = Compte.newBuilder()
                .setId(savedCompte.getId())
                .setSolde(savedCompte.getSolde())
                .setDateCreation(savedCompte.getDateCreation())
                .setType(TypeCompte.valueOf(savedCompte.getType()))
                .build();

        responseObserver.onNext(SaveCompteResponse.newBuilder().setCompte(grpcCompte).build());
        responseObserver.onCompleted();
}

    // Nouvelle méthode pour trouver des comptes par type
    @Override
    public void findByType(FindByTypeRequest request, StreamObserver<FindByTypeResponse> responseObserver) {
        TypeCompte type = request.getType();
        List<Compte> comptes = compteDB.values().stream()
                .filter(compte -> compte.getType() == type)
                .collect(Collectors.toList());

        FindByTypeResponse response = FindByTypeResponse.newBuilder()
                .addAllComptes(comptes)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Nouvelle méthode pour supprimer un compte par ID
    @Override
    public void deleteById(DeleteByIdRequest request, StreamObserver<DeleteByIdResponse> responseObserver) {
        String id = request.getId();
        boolean success = compteDB.remove(id) != null;

        DeleteByIdResponse response = DeleteByIdResponse.newBuilder()
                .setSuccess(success)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}