# Kotlin Language Server

## Desired features

### Completions
1. Contextual variable name completion based on the global index
   If SomeClass exists in the index, typing `som` should give the following completion `someClass: SomeClass`, although
   this should apply only where applicable (after `val/var` or in the method variables)
2. Auto-import after completing an item
