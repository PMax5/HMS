import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;


@Contract(
        name = "data",
        info = @Info(
                title = "Data Contract",
                description = "HMS Data Contract",
                version = "1.0",
                contact = @Contact(
                        email = "servers@pedromax.pt",
                        name = "HMS"
                )
        )
)
@Default
public final class Data implements ContractInterface {
}
