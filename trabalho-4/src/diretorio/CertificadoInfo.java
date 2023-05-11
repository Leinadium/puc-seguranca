package diretorio;

import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
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

    public static CertificadoInfo fromCertificado(X509Certificate cert) throws Exception {
        CertificadoInfo info = new CertificadoInfo();
        info.versao = cert.getVersion();
        info.serie = cert.getSerialNumber().toString();
        info.validade = cert.getNotBefore() + " - " + cert.getNotAfter();
        info.tipoAssinatura = cert.getSigAlgName();

        // pegando o nome e email do sujeito usando regex
        // common name = 2.5.4.3
        // email = 1.2.840.113549.1.9.1
        Certificate cert2 = Certificate.getInstance(cert.getEncoded());
        info.nomeEmissor = getFromSubject(cert2.getIssuer(), "2.5.4.3");
        info.nomeSujeito = getFromSubject(cert2.getSubject(), "2.5.4.3");
        info.emailSujeito = getFromSubject(cert2.getSubject(), "1.2.840.113549.1.9.1");
        return info;
    }

    static String getFromSubject(X500Name name, String oid) {
        RDN[] x = name.getRDNs(ASN1ObjectIdentifier.getInstance(oid));
        if (x.length > 0) {
            return x[0].getFirst().getValue().toString();
        } else {
            return "not-found";
        }
    }


}
