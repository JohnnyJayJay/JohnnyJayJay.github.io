module PieALaMode where

import Control.Monad
import Control.Concurrent.STM
import qualified Data.Map as Map
import Data.Maybe
import Data.Functor
import System.IO.Unsafe

data Ingredient = PieSlice | IceCreamScoop
  deriving (Eq, Ord)
data MenuItem = Pie | IceCream | PieALaMode
  deriving (Eq, Ord)

type Inventory = Map.Map Ingredient (TVar Int)
type Order = Map.Map MenuItem Int
type Ingredients = Map.Map Ingredient Int

recipes :: Map.Map MenuItem Ingredients
recipes = Map.fromList [(Pie, Map.singleton PieSlice 1),
                        (IceCream, Map.singleton IceCreamScoop 2),
                        (PieALaMode, Map.fromList [(PieSlice, 1), (IceCreamScoop, 1)])]

ingredientsFromOrder :: Order -> Ingredients
ingredientsFromOrder order = foldl (Map.unionWith (+)) Map.empty $ map toIngredients $ Map.toList order
  where toIngredients (item, quantity) =
          Map.map (* quantity) $ fromMaybe Map.empty $ Map.lookup item recipes


enoughInventory :: Inventory -> Ingredients -> STM Bool
enoughInventory inv ingredients = fmap (all id) $ mapM enoughIngredient $ Map.toList ingredients
  where enoughIngredient (ingredient, quantity) = fmap (> quantity) $ readTVar $ inv Map.! ingredient

handleOrder :: Inventory -> Order -> IO Bool
handleOrder inv order = let ingredients = ingredientsFromOrder order in
  atomically $ do
        enough <- enoughInventory inv ingredients
        when enough $ mapM_ takeIngredient $ Map.toList ingredients
        pure enough
  where takeIngredient (ingredient, quantity) = let var = inv Map.! ingredient in
          (readTVar var) <&> (-quantity +) >>= (writeTVar var)

handleOrderRetry :: Inventory -> Order -> IO ()
handleOrderRetry inv order = let ingredients = ingredientsFromOrder order in
  atomically $ do
        enough <- enoughInventory inv ingredients
        if enough then mapM_ takeIngredient $ Map.toList ingredients else retry
  where takeIngredient (ingredient, quantity) = let var = inv Map.! ingredient in
          (readTVar var) <&> (-quantity +) >>= (writeTVar var)

{-# NOINLINE sampleInventory #-}
sampleInventory :: Inventory
sampleInventory = unsafePerformIO $ mapM newTVarIO $ Map.fromList [(PieSlice, 12), (IceCreamScoop, 50)]
