package diretorio;

import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
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

        Certificate cert2 = Certificate.getInstance(cert.getEncoded());
        info.nomeEmissor = getFromSubject(cert2.getIssuer(), BCStyle.CN);
        info.nomeSujeito = getFromSubject(cert2.getSubject(), BCStyle.CN);
        info.emailSujeito = getFromSubject(cert2.getSubject(), BCStyle.EmailAddress);
        return info;
    }

    static String getFromSubject(X500Name name, ASN1ObjectIdentifier oid) {
        RDN[] x = name.getRDNs(oid);
        if (x.length > 0) {
            return x[0].getFirst().getValue().toString();
        } else {
            return "not-found";
        }
    }


}
