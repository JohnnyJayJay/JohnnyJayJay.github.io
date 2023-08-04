
import qualified Data.Set as Set
import Data.Set (Set, (\\))
import qualified Data.Tree as Tree
import Data.Tree (Tree)

bfs :: (Ord a) => Tree a -> a -> Bool
bfs root goal = bfs' (Set.singleton root) Set.empty goal

bfs' :: (Ord a) => Set (Tree a) -> Set (Tree a) -> a -> Bool
bfs' fringe visited goal
    | Set.null fringe = False
    | not $ Set.null $ Set.filter (== goal) $ Set.map Tree.rootLabel fringe = True
    | otherwise = bfs' ((Set.fromList [child | node <- (Set.elems fringe), child <- Tree.subForest node]) \\ visited)
                       (Set.union visited fringe)
                       goal
