createtable oneaa_warehaus

grant Table.READ -t oneaa_warehaus -u ezbake
grant System.CREATE_TABLE -s -u ezbake
grant System.DROP_TABLE -s -u ezbake
grant System.ALTER_TABLE -s -u ezbake

deleteiter -all -n org.apache.accumulo.core.iterators.user.VersioningIterator -t oneaa_warehaus

insert 0f66234664d7c14fde7cdf437a8820120f951ad6477f923e555752ed35a80b57:TESTING://numbers/Import1-UUIDfor-1 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 0f66234664d7c14fde7cdf437a8820120f951ad6477f923e555752ed35a80b57:TESTING://numbers/Import1-UUIDfor-1 TESTING://numbers/ RAW 1 -l U
insert 11c04768f03a1ee3975efdf014d1cb74ba88927b408a1f08840fed801b2b6ed2:TESTING://numbers/Import1-UUIDfor-2 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 11c04768f03a1ee3975efdf014d1cb74ba88927b408a1f08840fed801b2b6ed2:TESTING://numbers/Import1-UUIDfor-2 TESTING://numbers/ RAW 2 -l U
insert 3b69a5293412ed252321f4bda0639dd3173500858630c15fc2abc4e81f833ddd:TESTING://numbers/Import1-UUIDfor-3 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 3b69a5293412ed252321f4bda0639dd3173500858630c15fc2abc4e81f833ddd:TESTING://numbers/Import1-UUIDfor-3 TESTING://numbers/ RAW 3 -l U
insert 34ee8fa0e13873bd58f467c92151b4135a1814bd4acfbf275a5e472f6a854657:TESTING://numbers/Import1-UUIDfor-4 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 34ee8fa0e13873bd58f467c92151b4135a1814bd4acfbf275a5e472f6a854657:TESTING://numbers/Import1-UUIDfor-4 TESTING://numbers/ RAW 4 -l U
insert 6520578391f2d918b43e34ee2b2a0647fc7a68664d581866b6ff29d9cf50e097:TESTING://numbers/Import1-UUIDfor-5 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 6520578391f2d918b43e34ee2b2a0647fc7a68664d581866b6ff29d9cf50e097:TESTING://numbers/Import1-UUIDfor-5 TESTING://numbers/ RAW 5 -l U
insert da5f042c098475d24bce3117ad9c57075b5eca45e3789e442adbeb890947bd4b:TESTING://numbers/Import1-UUIDfor-6 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert da5f042c098475d24bce3117ad9c57075b5eca45e3789e442adbeb890947bd4b:TESTING://numbers/Import1-UUIDfor-6 TESTING://numbers/ RAW 6 -l U
insert 77c9f72aa7ecb98772b6ce4679dd85e37cfce6beb34d5b17862a18ee38f17429:TESTING://numbers/Import1-UUIDfor-7 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 77c9f72aa7ecb98772b6ce4679dd85e37cfce6beb34d5b17862a18ee38f17429:TESTING://numbers/Import1-UUIDfor-7 TESTING://numbers/ RAW 7 -l U
insert 831d9c39ac2ff9d4809f8e275f7276f7b778ad2a0b2ae2e8b5af3e4693214b14:TESTING://numbers/Import1-UUIDfor-8 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 831d9c39ac2ff9d4809f8e275f7276f7b778ad2a0b2ae2e8b5af3e4693214b14:TESTING://numbers/Import1-UUIDfor-8 TESTING://numbers/ RAW 8 -l U
insert 6cdf45bff7e392190fd92dcb58addddf9d317a232bd10274585a76dd06ddf006:TESTING://numbers/Import1-UUIDfor-9 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 6cdf45bff7e392190fd92dcb58addddf9d317a232bd10274585a76dd06ddf006:TESTING://numbers/Import1-UUIDfor-9 TESTING://numbers/ RAW 9 -l U
insert 16a3918240528c616452686d917af139b42a7be4f76cad9e4c03cb77f20b28ed:TESTING://numbers/Import1-UUIDfor-10 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 16a3918240528c616452686d917af139b42a7be4f76cad9e4c03cb77f20b28ed:TESTING://numbers/Import1-UUIDfor-10 TESTING://numbers/ RAW 10 -l U
insert 84e2e006836b56f8ab962c1cff776fc675af124b0af4135bb8e32f9d0104f02b:TESTING://numbers/Import1-UUIDfor-11 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 84e2e006836b56f8ab962c1cff776fc675af124b0af4135bb8e32f9d0104f02b:TESTING://numbers/Import1-UUIDfor-11 TESTING://numbers/ RAW 11 -l U
insert 20c57e16476ec3bb0baa24f8454c7647641622484a8d408c46b78fb91b2b3991:TESTING://numbers/Import1-UUIDfor-12 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 20c57e16476ec3bb0baa24f8454c7647641622484a8d408c46b78fb91b2b3991:TESTING://numbers/Import1-UUIDfor-12 TESTING://numbers/ RAW 12 -l U
insert fc04fcf4359df59e3adef0cc2d0be6ccbe6cfae2678f01748f50883e8830b556:TESTING://numbers/Import1-UUIDfor-13 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert fc04fcf4359df59e3adef0cc2d0be6ccbe6cfae2678f01748f50883e8830b556:TESTING://numbers/Import1-UUIDfor-13 TESTING://numbers/ RAW 13 -l U
insert 889bb7fd4752c344a86bafc80c0394fa85c207cdce9c2d9e9e81139b11b8f50a:TESTING://numbers/Import1-UUIDfor-14 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 889bb7fd4752c344a86bafc80c0394fa85c207cdce9c2d9e9e81139b11b8f50a:TESTING://numbers/Import1-UUIDfor-14 TESTING://numbers/ RAW 14 -l U
insert 9c66b51635628688ddf41a4c89bbfe83e13de37d328a77cc053b68dd8bfd490f:TESTING://numbers/Import1-UUIDfor-15 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 9c66b51635628688ddf41a4c89bbfe83e13de37d328a77cc053b68dd8bfd490f:TESTING://numbers/Import1-UUIDfor-15 TESTING://numbers/ RAW 15 -l U
insert 9814dec1b7d22f66fcac480ad4c4285a628fb57682d02999694766e44fe18ea8:TESTING://numbers/Import1-UUIDfor-16 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 9814dec1b7d22f66fcac480ad4c4285a628fb57682d02999694766e44fe18ea8:TESTING://numbers/Import1-UUIDfor-16 TESTING://numbers/ RAW 16 -l U
insert fec27dc94ce71e543dc196560c4d3f9105c680cfc32311e28b82fe6f459a51ef:TESTING://numbers/Import1-UUIDfor-17 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert fec27dc94ce71e543dc196560c4d3f9105c680cfc32311e28b82fe6f459a51ef:TESTING://numbers/Import1-UUIDfor-17 TESTING://numbers/ RAW 17 -l U
insert 0c36d6add1cf9c40eee97277cadbec4f1224af1dfac2bfd0787ae8e105a20e8e:TESTING://numbers/Import1-UUIDfor-18 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 0c36d6add1cf9c40eee97277cadbec4f1224af1dfac2bfd0787ae8e105a20e8e:TESTING://numbers/Import1-UUIDfor-18 TESTING://numbers/ RAW 18 -l U
insert f0234465bbf8b3299085fe4276b24df47461acc04f2fffca0d9a7110ab033e0f:TESTING://numbers/Import1-UUIDfor-19 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert f0234465bbf8b3299085fe4276b24df47461acc04f2fffca0d9a7110ab033e0f:TESTING://numbers/Import1-UUIDfor-19 TESTING://numbers/ RAW 19 -l U
insert 66be83f7c1eea47ace13eac1dc2c618fa324cd4ab1b66e028bfed70523f377a4:TESTING://numbers/Import1-UUIDfor-20 TESTING://numbers/ PARSED SOME_THRIFT_OBJ -l U
insert 66be83f7c1eea47ace13eac1dc2c618fa324cd4ab1b66e028bfed70523f377a4:TESTING://numbers/Import1-UUIDfor-20 TESTING://numbers/ RAW 20 -l U
quit
