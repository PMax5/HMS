use std::io::Result;
fn main() -> Result<()> {
    // TODO: Get a list of all the proto files dynamically.
    prost_build::compile_protos(&["src/config.proto"], &["src/"])?;
    Ok(())
}