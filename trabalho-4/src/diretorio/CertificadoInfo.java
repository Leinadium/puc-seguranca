package diretorio;

import org.bouncycastle.asn1.x509.Certificate;

import java.util.regex.Pattern;

public class CertificadoInfo {
    public int versao;
    public String serie;
    public String validade;
    public String tipoAssinatura;
    public String nomeEmissor;
    public String nomeSujeito;
    public String emailSujeito;

    public static CertificadoInfo fromCertificado(Certificate cert) {
        CertificadoInfo info = new CertificadoInfo();
        info.versao = cert.getVersion().getValue().intValue();
        info.serie = cert.getSerialNumber().toString();
        info.validade = cert.getStartDate() + " - " + cert.getEndDate();
        info.tipoAssinatura = cert.getSignatureAlgorithm().toString();

        // pegando o nome do emissor usando regex
        String emissor = cert.getIssuerX500Principal().getName("RFC1779");
        System.out.println(emissor);
        Pattern pattern = Pattern.compile("CN=(.*)/");
        info.nomeEmissor = pattern.matcher(emissor).group(1);

        // pegando o nome e email do sujeito usando regex
        String sujeito = cert.getSubjectX500Principal().getName("RFC1779");
        System.out.println(emissor);
        Pattern pattern2 = Pattern.compile("CN=(.*)/emailAddress=(.*),?$");
        info.nomeSujeito = pattern2.matcher(sujeito).group(1);
        info.emailSujeito = pattern2.matcher(sujeito).group(2);
        return info;
    }
}
